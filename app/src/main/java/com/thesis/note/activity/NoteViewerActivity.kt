package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
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

    var parameters: Bundle? = null
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

        //Load Note
        parameters = intent.extras

        //TODO loading groups, tags, date, itp
        if(parameters!=null){
            noteID = parameters!!.getInt("noteID")
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

        //remove button
        deleteButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                GlobalScope.launch {
                    val db = AppDatabase(applicationContext)
                    db.noteDao().delete(note)
                }
                finish()
            }
        })
        //save button
        //TODO save button

        //share button
        //TODO share button


    }

    override fun onResume() {
        super.onResume()
        //update dataList
        if(parameters!=null) {
            GlobalScope.launch {
                //load data form db
                dataList = db.dataDao().getDataFromNote(noteID)
                //set new data to recycler view
                viewAdapter = NoteViewerAdapter(dataList, NoteViewerActivityContext)
                runOnUiThread{
                    recyclerView.adapter = viewAdapter
                    viewAdapter.notifyDataSetChanged()
                }}}}

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

        when(viewAdapter.getItemViewType(position)){
            //NoteType.Text
            0 -> {
                val textEditorActivityIntent = Intent(this, TextEditorActivity::class.java)
                textEditorActivityIntent.putExtra("noteID",noteID)
                textEditorActivityIntent.putExtra("dataID",dataList[position].IdData)
                this.startActivity(textEditorActivityIntent)
            }
            else -> {
                Toast.makeText(applicationContext,"ERROR:NoteViewerActivity - cannot open data", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
