package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer

import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_add_note.*
import kotlinx.android.synthetic.main.activity_add_note.navigationView
import kotlinx.android.synthetic.main.activity_add_note.toolbar
import com.thesis.note.R
import kotlinx.android.synthetic.main.activity_debug.*

//TODO
class AddNoteActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        drawer_layout = add_note_drawer_layout;
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------

        val TextNoteIntent = Intent(this,TextEditorActivity::class.java)
        button.setOnClickListener{
                startActivity(TextNoteIntent)
                finish()
            }

        val ImageNoteIntent = Intent(this, ImageNoteActivity::class.java)
        ImageNoteIntent.putExtra("dataID", -1)
        ImageNoteIntent.putExtra("noteID", -1)
        button4.setOnClickListener {
            startActivity(ImageNoteIntent)
            finish()
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
}
