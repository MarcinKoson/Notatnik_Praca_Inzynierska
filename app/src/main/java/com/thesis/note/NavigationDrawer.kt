package com.thesis.note

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView
import com.thesis.note.activity.GroupsEditorActivity
import com.thesis.note.activity.MainActivity
import com.thesis.note.activity.SettingsActivity
import com.thesis.note.activity.TagEditorActivity
import com.thesis.note.fragment.AddNoteFragment
import com.thesis.note.fragment.SearchFragment
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
                Intent(context,GroupsEditorActivity::class.java).run{
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
                Intent(context,SettingsActivity::class.java).run{
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