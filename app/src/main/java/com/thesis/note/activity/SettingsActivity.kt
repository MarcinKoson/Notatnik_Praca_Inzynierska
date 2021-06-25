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
import kotlinx.android.synthetic.main.activity_debug.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.template_empty_layout.*
import kotlinx.android.synthetic.main.template_empty_layout.navigationView
import kotlinx.android.synthetic.main.template_empty_layout.toolbar
import com.thesis.note.R
//TODO
class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setSupportActionBar(toolbar)
        setContentView(R.layout.activity_settings)      //NAZWA LAYOUTU
        drawer_layout = settings_drawer_layout;               //NAZWA DRAWER LAYOUTU
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        val groupsEditorIntent = Intent(this, GroupsEditorActivity::class.java)
        groupsEditor.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(groupsEditorIntent)
            }})



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
