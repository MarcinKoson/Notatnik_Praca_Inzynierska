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
import kotlinx.android.synthetic.main.template_empty_layout.navigationView
import kotlinx.android.synthetic.main.template_empty_layout.toolbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.thesis.note.R
import com.thesis.note.TagsEditorRecyclerViewAdapter
import com.thesis.note.database.entity.Tag
import kotlinx.android.synthetic.main.activity_groups_editor.addTagButtonToDb
import kotlinx.android.synthetic.main.activity_groups_editor.nameOfNewTag
import kotlinx.android.synthetic.main.activity_tags_editor.*

//TODO
class TagEditorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    TagsEditorRecyclerViewAdapter.OnNoteListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    lateinit var db :AppDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val contextThis = this

    private lateinit var listOfTags: List<Tag>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags_editor)
        drawerLayout = tags_editor_layout
        navigationDrawer = NavigationDrawer(drawerLayout)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase.invoke(this)
        GlobalScope.launch {
            listOfTagsUpdate()

            viewManager = LinearLayoutManager(contextThis)
            viewAdapter = TagsEditorRecyclerViewAdapter(listOfTags,contextThis, contextThis)
            recyclerView = findViewById<RecyclerView>(R.id.groups_recycler_view).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

        }
        //------------------------add Button-----------------------------------------
        addTagButtonToDb.setOnClickListener {
            GlobalScope.launch {
                db.tagDao().insertAll(Tag(0, nameOfNewTag.text.toString()))
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
                TagsEditorRecyclerViewAdapter(listOfTags, contextThis as TagsEditorRecyclerViewAdapter.OnNoteListener,contextThis)
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
