package com.thesis.note

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.activity.AddNoteActivity
import com.thesis.note.activity.ListActivity
import com.thesis.note.activity.SettingsActivity
import androidx.core.content.ContextCompat.startActivity
import com.thesis.note.activity.MainActivity
import com.thesis.note.R


class NavigationDrawer(drawer_layout: DrawerLayout) {
    val drawer_layout = drawer_layout

    fun onNavigationItemSelected(menuItem: MenuItem,context: Context): Boolean {

        when (menuItem.itemId) {
            R.id.start ->{
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }

            R.id.drawer_list -> {
                val listActivityIntent = Intent(context, ListActivity::class.java)

                context.startActivity(listActivityIntent)
            }
            R.id.add_note ->{
                val addNoteActivityIntent = Intent(context,AddNoteActivity::class.java)
                context.startActivity(addNoteActivityIntent)
            }
            R.id.drawer_settings ->
            {
                val settingsIntent = Intent(context,SettingsActivity::class.java)
                context.startActivity(settingsIntent)
            }
            R.id.drawer_debug ->
            {
                val debugActivityIntent = Intent(context, DebugActivity::class.java)
                context.startActivity(debugActivityIntent);
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }







}