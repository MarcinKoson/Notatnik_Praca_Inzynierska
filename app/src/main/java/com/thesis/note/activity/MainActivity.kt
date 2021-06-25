package com.thesis.note.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.thesis.note.RecyclerViewAdapter
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.thesis.note.R
//TODO
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener ,
    RecyclerViewAdapter.OnNoteListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    //lista favorite
    lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var contextThis: Context
    private lateinit var listOfNotes: List<Note>
    private lateinit var listOfData: List<Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setSupportActionBar(toolbar)  //test
        setContentView(R.layout.activity_main)
        drawer_layout = activity_main_layout;
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase(this)
        //todo - untested bugged code
        /*

        val contextThisX = this
        contextThis = this
        GlobalScope.launch {
            listOfNotes = db.noteDao().getFavorite(true)
            listOfData = db.dataDao().getAll()

            viewManager = LinearLayoutManager(contextThis)
            viewAdapter = RecyclerViewAdapter(listOfNotes,listOfData,contextThisX)

            recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view).apply {

                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
*/
        //ADD button

        addButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v?.context,AddNoteActivity::class.java)
                startActivity(intent)
            }
        })

    }

    override fun onRestart() {
        super.onRestart()
        /*
        GlobalScope.launch {
            listOfNotes = db.noteDao().getFavorite(true)
            viewAdapter =
                RecyclerViewAdapter(listOfNotes, listOfData,contextThis as RecyclerViewAdapter.OnNoteListener)
            runOnUiThread {
            recyclerView.setAdapter(viewAdapter)
            viewAdapter.notifyDataSetChanged()
        }

    }
       // viewAdapter.myDa
       */

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {

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

    }

}

