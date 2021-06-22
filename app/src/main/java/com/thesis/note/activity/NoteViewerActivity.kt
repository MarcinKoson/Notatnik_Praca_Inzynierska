package com.thesis.note.activity

import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.NavigationDrawer

import com.google.android.material.navigation.NavigationView
import com.thesis.note.NoteViewerAdapter

import kotlinx.android.synthetic.main.template_empty_layout.navigationView
import kotlinx.android.synthetic.main.template_empty_layout.toolbar
import com.thesis.note.R

import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import kotlinx.android.synthetic.main.activity_note_viewer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class NoteViewerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NoteViewerAdapter.OnNoteListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    val db = AppDatabase.invoke(this)
    val NoteViewerActivityContext = this

    var noteID:Int = -1
    lateinit var note:Note
    lateinit var dataList: List<Data>

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setSupportActionBar(toolbar)
        setContentView(R.layout.activity_note_viewer)
        drawer_layout = activity_note_viewer_layout
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------

        //Checking note ID
        val parameters = intent.extras

        if(parameters!=null){
            noteID = parameters.getInt("noteID")
            //Get note from db
            GlobalScope.launch {
                note = db.noteDao().getNoteById(noteID)
                dataList = db.dataDao().getDataFromNote(noteID)

                //Setting note name
                NoteViewerActivityContext.runOnUiThread(
                    fun(){
                        noteName.text = Editable.Factory.getInstance().newEditable(note.Name)
                    }
                )
                //RecyclerView init
                viewManager = LinearLayoutManager(NoteViewerActivityContext)
                viewAdapter = NoteViewerAdapter(dataList,NoteViewerActivityContext)

                recyclerView = findViewById<RecyclerView>(R.id.note_viewer_recycler_view).apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }
        }
        else
        {
            Toast.makeText(applicationContext,"ERROR: cannot load note", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNoteClick(position: Int) {
        //TODO ("Not yet implemented")
        //
    }
}
