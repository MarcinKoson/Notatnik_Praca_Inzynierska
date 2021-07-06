package com.thesis.note.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.thesis.note.recycler_view_adapters.RecyclerViewAdapter
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.R
import com.thesis.note.SearchValuesS
import com.thesis.note.recycler_view_adapters.NoteListAdapter
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.activity_main.addButton
import kotlinx.android.synthetic.main.activity_main.navigationView
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//TODO
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener ,
    NoteListAdapter.OnNoteClickListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    //lista favorite
    lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private  val contextThis = this
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
        //TODO remove temp list
        //Database
        db = AppDatabase.invoke(this)

        GlobalScope.launch {
            listOfNotesUpdate()
            viewManager = LinearLayoutManager(contextThis)
            viewAdapter = NoteListAdapter(listOfNotes,listOfData,contextThis as NoteListAdapter.OnNoteClickListener)
            recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

            //If no notes show message
            if(listOfNotes.isEmpty()){
                listActivityMessage2.visibility = android.view.View.VISIBLE
            }
        }



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

        GlobalScope.launch {
            //listOfNotes = db.noteDao().getAll()
            listOfNotesUpdate()
            viewAdapter =
                NoteListAdapter(listOfNotes, listOfData, contextThis)
            runOnUiThread {
                recyclerView.setAdapter(viewAdapter)
                viewAdapter.notifyDataSetChanged()
            }

            runOnUiThread {
                //If no notes show message
                if (listOfNotes.isEmpty()) {
                    listActivityMessage2.visibility = View.VISIBLE
                } else
                    listActivityMessage2.visibility = View.GONE
            }
        }

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

        val noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
        noteViewerActivityIntent.putExtra("noteID",listOfNotes[position].IdNote);
        this.startActivity(noteViewerActivityIntent)

    }

    @Deprecated("only for temporary list")
    fun listOfNotesUpdate(){
        var favorite:MutableList<Boolean> = mutableListOf()
        if(SearchValuesS.favorite)
        {

            favorite.add(true)
        }
        else{

            favorite.add(true)
            favorite.add(false)
        }
        var nameReg = SearchValuesS.name;
        if(nameReg==null || nameReg==""){
            nameReg = "%"
        }

        var groups:MutableList<Int?> = mutableListOf()
        val groupsList = db.groupDao().getAll()
        if(SearchValuesS.group == 0 || SearchValuesS.group ==null){
            // groups = groupsList.map { it.IdGroup }.toMutableList();
            //groups.add(null);
            listOfNotes = db.noteDao().getFiltered(favorite,nameReg.toString())
        }
        else{
            val groups:MutableList<Int?> = mutableListOf()
            //groups.add(null);
            var findGroupID:Int = SearchValuesS.group!!
            groups.add(groupsList[findGroupID-1].IdGroup)
            listOfNotes = db.noteDao().getFilteredGroup(groups,favorite,nameReg.toString())
        }

        //--------------Data load--------------
        listOfData = db.dataDao().getAll()

    }



}

