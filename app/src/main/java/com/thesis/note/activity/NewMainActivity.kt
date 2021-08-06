package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import com.thesis.note.R
import com.thesis.note.SearchValuesS
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Group
import com.thesis.note.database.entity.Note
import com.thesis.note.database.entity.Tag
import com.thesis.note.databinding.ActivityNewMainBinding
import com.thesis.note.recycler_view_adapters.NoteListAdapter
import com.thesis.note.recycler_view_adapters.NoteTilesListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class NewMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityNewMainBinding

    private val contextThis = this

    lateinit var db: AppDatabase
    private lateinit var listOfNotes: List<Note>
    private lateinit var listOfData: List<Data>

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityNewMainLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle = ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        checkFirstStart()
        //Load notes from
        db = AppDatabase(this)
        //Load data
        GlobalScope.launch {
            listOfData = db.dataDao().getAll()
        }
        //Add button
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(it.context,AddNoteActivity::class.java)
            startActivity(intent)
        }



        //Load notes
        //Search values
        val favorite: MutableList<Boolean> = if(SearchValuesS.favorite)
            mutableListOf(true)
        else
            mutableListOf(true,false)
        //Name
        var nameReg = SearchValuesS.name
        if(nameReg==null || nameReg==""){
            nameReg = "%"
        }
        //Nove viewer intent
        val noteViewerActivityIntent = Intent(contextThis, NoteViewerActivity::class.java)
        //Groups
        GlobalScope.launch {
            listOfNotes = if (SearchValuesS.group == null) {
                db.noteDao().getFiltered(favorite, nameReg.toString())
            } else {
                val groups: MutableList<Int?> = mutableListOf(SearchValuesS.group)
                db.noteDao().getFilteredGroup(groups, favorite, nameReg.toString())
            }
        //init recycler view

        viewManager = GridLayoutManager(contextThis, 2)
        viewAdapter = NoteTilesListAdapter(listOfNotes, listOfData,
            object : NoteTilesListAdapter.OnNoteClickListener {
                override fun onNoteClick(position: Int) {
                    noteViewerActivityIntent.putExtra(
                        "noteID",
                        listOfNotes[position].IdNote
                    );
                    contextThis.startActivity(noteViewerActivityIntent)
                }
            })
        recyclerView = binding.noteTilesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }}

    }

    override fun onResume() {
        super.onResume()
      //  Toast.makeText(applicationContext, "onResume", Toast.LENGTH_SHORT).show()
        //load data
        GlobalScope.launch {
        listOfData = db.dataDao().getAll()}
        //Load notes

        //Search values
        val favorite: MutableList<Boolean> = if(SearchValuesS.favorite)
            mutableListOf(true)
        else
            mutableListOf(true,false)
        //Name
        var nameReg = SearchValuesS.name
        if(nameReg==null || nameReg==""){
            nameReg = "%"
        }
        //Nove viewer intent
        val noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
        //Groups
        GlobalScope.launch {
            listOfNotes = if (SearchValuesS.group == null) {
                db.noteDao().getFiltered(favorite, nameReg.toString())
            } else {
                val groups: MutableList<Int?> = mutableListOf(SearchValuesS.group)
                db.noteDao().getFilteredGroup(groups, favorite, nameReg.toString())
            }
            //update recycler view
            runOnUiThread {
                viewAdapter = NoteTilesListAdapter(listOfNotes, listOfData,
                    object : NoteTilesListAdapter.OnNoteClickListener {
                        override fun onNoteClick(position: Int) {
                            noteViewerActivityIntent.putExtra(
                                "noteID",
                                listOfNotes[position].IdNote
                            );
                            contextThis.startActivity(noteViewerActivityIntent)
                        }
                    })
                recyclerView.setAdapter(viewAdapter)
                viewAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkFirstStart() {
        val sharedPrefs = getSharedPreferences("appSharedPrefs",MODE_PRIVATE)
        val notFirstStart = sharedPrefs.getBoolean("notFirstStart", false)
        if(!notFirstStart){
            GlobalScope.launch {
                val db = AppDatabase(contextThis)
                db.groupDao().insertAll(Group(0,"Grupa 1",null))
                db.groupDao().insertAll(Group(0,"Grupa 2",null))
                db.groupDao().insertAll(Group(0,"Grupa 3",null))
                db.tagDao().insertAll(Tag(0,"Tag 1"))
                db.tagDao().insertAll(Tag(0,"Tag 2"))
                db.tagDao().insertAll(Tag(0,"Tag 3"))

                var note = db.noteDao().insertAll(Note(0,"Note",null,null,false,null,null,null,NoteColor.Cyan))
                var data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example",null,16,NoteColor.Black))
                db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

                note = db.noteDao().insertAll(Note(0,"Bold",null,null,false,null,null,null,NoteColor.Teal))
                data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example","B",16,NoteColor.Purple))
                db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

                note = db.noteDao().insertAll(Note(0,"Italic",null,null,false,null,null,null,NoteColor.Yellow))
                data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example","I",16,NoteColor.Black))
                db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

            }


            sharedPrefs.edit().putBoolean("notFirstStart", true).apply()
        }
    }

}
