package com.thesis.note.activity

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.NavigationDrawer
import com.thesis.note.database.AppDatabase
import com.google.android.material.navigation.NavigationView

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.thesis.note.R
import com.thesis.note.recycler_view_adapters.TagsEditorAdapter
import com.thesis.note.database.entity.Tag
import com.thesis.note.databinding.ActivityTagsEditorBinding


//TODO documentation
class TagEditorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    TagsEditorAdapter.OnNoteListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityTagsEditorBinding

    lateinit var db :AppDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val contextThis = this

    private lateinit var listOfTags: List<Tag>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagsEditorBinding.inflate(layoutInflater) //LAYOUT BINDING CLASS
        setContentView(binding.root)
        drawerLayout = binding.activityTagsEditorLayout
        navigationDrawer = NavigationDrawer(drawerLayout,supportFragmentManager)
        binding.navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase.invoke(this)
        GlobalScope.launch {
            listOfTagsUpdate()

            viewManager = LinearLayoutManager(contextThis)
            viewAdapter = TagsEditorAdapter(listOfTags,contextThis, contextThis)
            recyclerView = findViewById<RecyclerView>(R.id.groups_recycler_view).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

        }
        //------------------------add Button-----------------------------------------
        binding.addTagButtonToDb.setOnClickListener {
            GlobalScope.launch {
                db.tagDao().insertAll(Tag(0, binding.nameOfNewTag.text.toString()))
                (contextThis as Activity).runOnUiThread {
                    contextThis.recreate()
                }
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
    override fun onNoteClick(position: Int) {
        }


    override fun onRestart() {
        super.onRestart()
        GlobalScope.launch {
            //listOfNotes = db.noteDao().getAll()
            listOfTagsUpdate()
            viewAdapter =
                TagsEditorAdapter(listOfTags, contextThis as TagsEditorAdapter.OnNoteListener,contextThis)
            runOnUiThread {
                recyclerView.adapter = viewAdapter
                viewAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun listOfTagsUpdate(){
            listOfTags = db.tagDao().getAll()
    }

}
