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
import com.thesis.note.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater) //LAYOUT BINDING CLASS
        setContentView(binding.root)
        drawerLayout = binding.activitySettingsLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        val groupsEditorIntent = Intent(this, GroupsEditorActivity::class.java)
        binding.groupsEditor.setOnClickListener { startActivity(groupsEditorIntent) }

        val tagsEditorIntent = Intent(this, TagEditorActivity::class.java)
        binding.tagsEditor.setOnClickListener { startActivity(tagsEditorIntent) }

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
}
