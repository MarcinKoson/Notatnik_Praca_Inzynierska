package com.thesis.note.activity

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.NavigationDrawer

import com.thesis.note.recycler_view_adapters.GroupsEditorAdapter
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Group
import com.google.android.material.navigation.NavigationView

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.thesis.note.R
import com.thesis.note.databinding.ActivityGroupsEditorBinding


//TODO documentation
class GroupsEditorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    GroupsEditorAdapter.OnNoteListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityGroupsEditorBinding

    lateinit var db :AppDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val contextThis = this

    private lateinit var listOfGroups: List<Group>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupsEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityGroupsEditorLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase.invoke(this);
        GlobalScope.launch {
            listOfGroupsUpdate()

            viewManager = LinearLayoutManager(contextThis)
            viewAdapter = GroupsEditorAdapter(listOfGroups,contextThis, contextThis)

            recyclerView = findViewById<RecyclerView>(R.id.groups_recycler_view).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }

        }
        //------------------------add Button-----------------------------------------
        binding.addTagButtonToDb.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                GlobalScope.launch {
                db.groupDao().insertAll(Group(0,binding.nameOfNewTag.text.toString(),null))
                    (contextThis as Activity).runOnUiThread {
                        contextThis.recreate()
                    }
                }
            }})







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
            listOfGroupsUpdate()
            viewAdapter =
                GroupsEditorAdapter(listOfGroups, contextThis as GroupsEditorAdapter.OnNoteListener,contextThis)
            runOnUiThread {
                recyclerView.setAdapter(viewAdapter)
                viewAdapter.notifyDataSetChanged()
            }
        }
    }

    fun listOfGroupsUpdate(){
            listOfGroups = db.groupDao().getAll()
    }

}
