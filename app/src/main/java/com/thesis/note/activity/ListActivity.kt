package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.*
import com.thesis.note.RecyclerViewAdapter.*
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_list.*

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.stream.Collectors.toList
import com.thesis.note.R

class ListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnNoteListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer


    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var db: AppDatabase

    private  val contextThis = this
    private lateinit var listOfNotes: List<Note>
    private lateinit var listOfData:List<Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setSupportActionBar(toolbar)
        setContentView(R.layout.activity_list)      //NAZWA LAYOUTU
        drawer_layout = list_layout;               //NAZWA DRAWER LAYOUTU
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        //Database
        db = AppDatabase.invoke(this)

        GlobalScope.launch {

            listOfNotesUpdate()

            //TODO("poprawic")
            viewManager = LinearLayoutManager(contextThis)
            viewAdapter = RecyclerViewAdapter(listOfNotes,listOfData,contextThis)

            recyclerView = findViewById<RecyclerView>(R.id.notes_recycler_view).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = viewManager

                // specify an viewAdapter (see also next example)
                adapter = viewAdapter
            }
        }
            //-------------------------
        //Add button

        addButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v?.context,AddNoteActivity::class.java)
                startActivity(intent)
            }
        })

        findButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(v?.context,SearchActivity::class.java)
                startActivity(intent)
            }
        })


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
        //DUBLICATE -> MainActivity
        /*
        val noteType = listOfNotes[position].Type;
        //TODO zmiana na noteviever
        when(noteType){
            NoteType.Text -> {
                val listActivityIntent = Intent(this, TextEditorActivity::class.java)
                listActivityIntent.putExtra("noteID",listOfNotes[position].IdNote);
                this.startActivity(listActivityIntent)
               // finish()
            }
            else -> {
                Toast.makeText(applicationContext,"error:nie obsługiwana notatka",Toast.LENGTH_SHORT).show()
            }
        }
*/


        val listActivityIntent = Intent(this, TextEditorActivity::class.java)
        listActivityIntent.putExtra("noteID",listOfNotes[position].IdNote);
        this.startActivity(listActivityIntent)

    }

    override fun onRestart() {
        super.onRestart()
        GlobalScope.launch {
            //listOfNotes = db.noteDao().getAll()
            listOfNotesUpdate()
            viewAdapter =
                RecyclerViewAdapter(listOfNotes, listOfData,contextThis as RecyclerViewAdapter.OnNoteListener)
            runOnUiThread {
                recyclerView.setAdapter(viewAdapter)
                viewAdapter.notifyDataSetChanged()
            }

        }
        // viewAdapter.myDa
    }

    fun listOfNotesUpdate(){
        val sv:SearchValues = SearchValues()

        val groups:MutableList<Int?> = db.groupDao().getAll().map { it.IdGroup }.toMutableList();
        groups.add(null);
        //TODO bug not finding notes without group(null)
        val favorite:MutableList<Boolean> = mutableListOf();
        favorite.add(true)
        favorite.add(false)

        //val nameReg = sv.name;
        val nameReg = SearchValuesS.name;

        if(nameReg==null || nameReg=="")
            listOfNotes = db.noteDao().getAll()
        else {
            //    listOfNotes = db.noteDao().getFiltered(groupsID = groups.toList(),favorite =  favorite.toList(), nameRegex = nameReg.toString())

        }

        //--------------Data load--------------
        listOfData = db.dataDao().getAll()

    }

}
