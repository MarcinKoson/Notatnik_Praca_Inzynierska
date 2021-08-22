package com.thesis.note

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import com.thesis.note.activity.*
import com.thesis.note.fragment.AddNoteFragment
import com.thesis.note.test.DebugActivity

/**
 * [NavigationView] drawer logic
 *
 */
class NavigationDrawer(val drawerLayout: DrawerLayout, private val supportFragmentManager: FragmentManager) {
    /** On navigation item selected */
    fun onNavigationItemSelected(menuItem: MenuItem,context: Context): Boolean {
        when (menuItem.itemId) {
            R.id.start ->{
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
            R.id.drawer_list -> {
                val listActivityIntent = Intent(context, MainActivity::class.java)
                context.startActivity(listActivityIntent)
            }
            R.id.add_note ->{
                context.run {
                    AddNoteFragment().show(supportFragmentManager,"add_note")
                }

            }
            R.id.drawer_settings ->
            {
                val settingsIntent = Intent(context,SettingsActivity::class.java)
                context.startActivity(settingsIntent)
            }
            R.id.drawer_debug ->
            {
                val debugActivityIntent = Intent(context, DebugActivity::class.java)
                context.startActivity(debugActivityIntent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}