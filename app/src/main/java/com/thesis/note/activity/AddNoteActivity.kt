package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer

import com.google.android.material.navigation.NavigationView

import com.thesis.note.R
import com.thesis.note.databinding.ActivityAddNoteBinding


//TODO
class AddNoteActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityAddNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawer_layout = binding.activityAddNoteLayout
        navigationDrawer = NavigationDrawer(drawer_layout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------

        val TextNoteIntent = Intent(this,TextEditorActivity::class.java)
        binding.button.setOnClickListener{
                startActivity(TextNoteIntent)
                finish()
            }

        val ImageNoteIntent = Intent(this, ImageNoteActivity::class.java)
        ImageNoteIntent.putExtra("dataID", -1)
        ImageNoteIntent.putExtra("noteID", -1)
        binding.button4.setOnClickListener {
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
