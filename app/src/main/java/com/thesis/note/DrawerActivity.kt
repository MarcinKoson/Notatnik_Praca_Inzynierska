package com.thesis.note

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

/**
 * Class for activity with drawer. It provide logic for drawer.
 */
abstract class DrawerActivity :
    AppCompatActivity()
{
    /** [DrawerLayout] of your activity */
    lateinit var drawerLayout: DrawerLayout

    /** [NavigationDrawer] of your activity */
    lateinit var navigationDrawer : NavigationDrawer

    /** Listener for item selected in [NavigationView] */
    val navigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener{
        finish()
        navigationDrawer.onNavigationItemSelected(it,this)
    }

    /** Logic for back button */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    /** Set content and drawer for activity */
    fun setDrawerLayout(drawerLayout: DrawerLayout, toolbar: Toolbar, navigationView: NavigationView){
        this.drawerLayout = drawerLayout
        setContentView(drawerLayout)
        navigationDrawer = NavigationDrawer(drawerLayout)
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener)
        ActionBarDrawerToggle(
            this,drawerLayout,toolbar,
            R.string.action_bar_drawer_toggle_open,R.string.action_bar_drawer_toggle_close
        ).apply {
            drawerLayout.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
    }
}