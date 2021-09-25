package com.thesis.note.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.thesis.note.R
import com.thesis.note.fragment.AddNoteFragment
import com.thesis.note.fragment.SearchFragment
import com.thesis.note.test.DebugActivity
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

    /** Set content and drawer for activity */
    fun setDrawerLayout(drawerLayout: DrawerLayout, toolbar: Toolbar, navigationView: NavigationView){
        this.drawerLayout = drawerLayout
        setContentView(drawerLayout)
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener)
        ActionBarDrawerToggle(
            this,drawerLayout,toolbar,
            R.string.action_bar_drawer_toggle_open, R.string.action_bar_drawer_toggle_close
        ).apply {
            drawerLayout.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        //load navigation drawer settings
        with(PreferenceManager.getDefaultSharedPreferences(this)){
            navigationView.menu.findItem(R.id.favorites).also { item ->
                with(this.getBoolean("navigation_drawer_favorites", true)) {
                    item.isEnabled = this
                    item.isVisible = this
                }
            }
            navigationView.menu.findItem(R.id.add_note).also { item ->
                with(this.getBoolean("navigation_drawer_add", true)) {
                    item.isEnabled = this
                    item.isVisible = this
                }
            }
            navigationView.menu.findItem(R.id.groups).also { item ->
                with(this.getBoolean("navigation_drawer_groups", true)) {
                    item.isEnabled = this
                    item.isVisible = this
                }
            }
            navigationView.menu.findItem(R.id.tags).also { item ->
                with(this.getBoolean("navigation_drawer_tags", true)) {
                    item.isEnabled = this
                    item.isVisible = this
                }
            }
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
        onNavigationItemSelected(it,this)
    }

    /** On navigation item selected */
    private fun onNavigationItemSelected(menuItem: MenuItem, context: Context): Boolean {
        when (menuItem.itemId) {
            R.id.start ->{
                Intent(context, MainActivity::class.java).run {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(this)
                }
            }
            R.id.favorites -> {
                Intent(context, MainActivity::class.java).run {
                    putExtra("search", SearchFragment.SearchValues().let { it.favorite = true; it.toString() })
                    context.startActivity(this)
                }
            }
            R.id.add_note ->{
                context.run {
                    AddNoteFragment().show(supportFragmentManager,"add_note")
                }
            }
            R.id.groups -> {
                Intent(context, GroupsEditorActivity::class.java).run{
                    context.startActivity(this)
                }
            }
            R.id.tags -> {
                Intent(context, TagEditorActivity::class.java).run{
                    context.startActivity(this)
                }
            }
            R.id.drawer_settings ->
            {
                Intent(context, SettingsActivity::class.java).run{
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(this)
                }
            }
            R.id.drawer_debug ->
            {
                Intent(context, DebugActivity::class.java).run {
                    context.startActivity(this)
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}
