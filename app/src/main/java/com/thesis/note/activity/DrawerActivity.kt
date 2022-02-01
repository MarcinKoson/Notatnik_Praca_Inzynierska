package com.thesis.note.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Class for activity with drawer. It provide logic for navigation drawer.
 * It sets theme and locale from settings in [onCreate].
 * To use inflate your layout, then run [setDrawerLayout].
 */
abstract class DrawerActivity :
    AppCompatActivity() {
    /** Loaded in [onCreate] */
    lateinit var sharedPreferences: SharedPreferences

    /** Set content and drawer for activity */
    fun setDrawerLayout(
        drawerLayout: DrawerLayout,
        toolbar: Toolbar,
        navigationView: NavigationView
    ) {
        this.drawerLayout = drawerLayout
        setContentView(drawerLayout)
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener)
        ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.action_bar_drawer_toggle_open, R.string.action_bar_drawer_toggle_close
        ).apply {
            drawerLayout.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        //load navigation drawer settings
        navigationView.menu.findItem(R.id.favorites).also { item ->
            with(sharedPreferences.getBoolean("navigation_drawer_favorites", true)) {
                item.isEnabled = this
                item.isVisible = this
            }
        }
        navigationView.menu.findItem(R.id.add_note).also { item ->
            with(sharedPreferences.getBoolean("navigation_drawer_add", true)) {
                item.isEnabled = this
                item.isVisible = this
            }
        }
        navigationView.menu.findItem(R.id.groups).also { item ->
            with(sharedPreferences.getBoolean("navigation_drawer_groups", true)) {
                item.isEnabled = this
                item.isVisible = this
            }
        }
        navigationView.menu.findItem(R.id.tags).also { item ->
            with(sharedPreferences.getBoolean("navigation_drawer_tags", true)) {
                item.isEnabled = this
                item.isVisible = this
            }
        }

    }

    /** On create callback. It initiate [sharedPreferences] then sets theme and language from settings. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setActivityTheme(sharedPreferences.getString("theme", ""))
        sharedPreferences.getString("language", "")?.let { setLocale(it) }
    }

    /** When pressing back button or choosing menu item in navigation drawer show dialog with alert before closing activity */
    var showDiscardChangesDialog = false

    /** Logic for back button */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if(showDiscardChangesDialog){
                AlertDialog.Builder(this).run{
                    setPositiveButton(R.string.activity_text_editor_discard_changes_positive) { _, _ ->
                        GlobalScope.launch {
                            runOnUiThread {
                                super.onBackPressed()
                            }
                        }
                    }
                    setNegativeButton(R.string.activity_text_editor_discard_changes_negative) { _, _ -> }
                    setTitle(R.string.activity_text_editor_discard_changes)
                    create()
                }.show()
            }
            else{
                super.onBackPressed()
            }
        }
    }

    /** [DrawerLayout] of activity */
    private lateinit var drawerLayout: DrawerLayout

    /** Set passed locale */
    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config: Configuration = resources.configuration.also {
            it.setLocale(locale)
        }
        this.resources.updateConfiguration(config, this.resources.displayMetrics)
    }

    /** Set passed theme */
    fun setActivityTheme(theme: String?){
        when (theme) {
            null -> setTheme(R.style.Theme_Note_Blue)
            "blue" -> setTheme(R.style.Theme_Note_Blue)
            "green" -> setTheme(R.style.Theme_Note_Green)
            "orange" -> setTheme(R.style.Theme_Note_Orange)
            "red" -> setTheme(R.style.Theme_Note_Red)
        }
    }

    /** Listener for item selected in [NavigationView] */
    private val navigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener {
        if(showDiscardChangesDialog){
            AlertDialog.Builder(this).run{
                setPositiveButton(R.string.activity_text_editor_discard_changes_positive) { _, _ ->
                    GlobalScope.launch {
                        runOnUiThread {
                            openMenuItem(it)
                            drawerLayout.closeDrawer(GravityCompat.START)
                        }
                    }
                }
                setNegativeButton(R.string.activity_text_editor_discard_changes_negative) { _, _ -> }
                setTitle(R.string.activity_text_editor_discard_changes)
                create()
            }.show()
        }
        else{
            openMenuItem(it)
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        true
    }

    /** */
    private fun openMenuItem(menuItem: MenuItem){
        when (menuItem.itemId) {
            R.id.start -> {
                Intent(this, MainActivity::class.java).run {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(this)
                }
            }
            R.id.favorites -> {
                Intent(this, MainActivity::class.java).run {
                    putExtra(
                        "search",
                        SearchFragment.SearchValues().let { sv -> sv.favorite = true; sv.toString() })
                    startActivity(this)
                }
            }
            R.id.add_note -> {
                this.run {
                    AddNoteFragment().show(supportFragmentManager, "add_note")
                }
            }
            R.id.groups -> {
                Intent(this, GroupsEditorActivity::class.java).run {
                    startActivity(this)
                }
            }
            R.id.tags -> {
                Intent(this, TagEditorActivity::class.java).run {
                    startActivity(this)
                }
            }
            R.id.drawer_settings -> {
                Intent(this, SettingsActivity::class.java).run {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(this)
                }
            }
            R.id.drawer_debug -> {
                Intent(this, TextWidgetEditorActivity::class.java).run {
                    startActivity(this)
                }
            }
        }
    }
}
