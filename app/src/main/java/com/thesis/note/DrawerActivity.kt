package com.thesis.note

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import java.util.*

/**
 * Class for activity with drawer. It provide logic for drawer.
 * It is setting layout and locale from settings in [onCreate].
 * Inflate your layout, then run [setDrawerLayout]
 */
abstract class DrawerActivity :
    AppCompatActivity()
{
    /** [DrawerLayout] of your activity */
    lateinit var drawerLayout: DrawerLayout

    /** [NavigationDrawer] of your activity */
    private lateinit var navigationDrawer : NavigationDrawer

    /** Set content and drawer for activity */
    fun setDrawerLayout(drawerLayout: DrawerLayout, toolbar: Toolbar, navigationView: NavigationView){
        this.drawerLayout = drawerLayout
        setContentView(drawerLayout)
        navigationDrawer = NavigationDrawer(drawerLayout,supportFragmentManager)
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

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        //set theme
        when(sharedPreferences.getString("theme", "")){
            null -> setTheme(R.style.Theme_Note_Blue)
            "blue" ->setTheme(R.style.Theme_Note_Blue)
            "green" ->setTheme(R.style.Theme_Note_Green)
            "orange" ->setTheme(R.style.Theme_Note_Orange)
            "red" ->setTheme(R.style.Theme_Note_Red)
        }
        //set locale
        when(sharedPreferences.getString("language", "")){
            null -> { }
            "pl" ->setLocale("pl")
            "en" ->setLocale("en")
        }
    }

    /** Set passed locale */
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config: Configuration = resources.configuration.also{
            it.setLocale(locale)
        }
        this.resources.updateConfiguration(config, this.resources.displayMetrics)
    }

    /** Logic for back button */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /** Listener for item selected in [NavigationView] */
    private val navigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener{
        navigationDrawer.onNavigationItemSelected(it,this)
    }
}