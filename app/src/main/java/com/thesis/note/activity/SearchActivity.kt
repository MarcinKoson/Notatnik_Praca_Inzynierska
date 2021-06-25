package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer

import com.thesis.note.SearchValues
import com.thesis.note.SearchValuesS
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.navigationView
import kotlinx.android.synthetic.main.activity_search.toolbar
import kotlinx.android.synthetic.main.activity_text_editor.*
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Group
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//TODO
class SearchActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    lateinit var groupsList: List<Group>
    val db = AppDatabase.invoke(this)
    val SearchActivityContext = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setSupportActionBar(toolbar)
        setContentView(R.layout.activity_search)      //NAZWA LAYOUTU
        drawer_layout = search_drawer_layout;               //NAZWA DRAWER LAYOUTU
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()

        searchButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var sv = SearchValues()
                sv.name = editText.text.toString()

                SearchValuesS.name = "%"+editText.text.toString()+"%"
                SearchValuesS.favorite = checkBox.isChecked
                SearchValuesS.group = groupSpinner2.selectedItemPosition

                finish()
            }
        })

        val spinner: Spinner = groupSpinner2

        GlobalScope.launch {
            var arrayGroups = db.groupDao().getAll()
            groupsList = arrayGroups
            var arrayGroupsString = arrayGroups.map { x -> x.Name }

            var groupArrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                SearchActivityContext,
                android.R.layout.simple_spinner_item,
                arrayGroupsString
            )

            groupArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            //groupArrayAdapter.insert(getString(R.string.groups_without_group), 0)
            groupArrayAdapter.insert("dowolna", 0)
            spinner.adapter = groupArrayAdapter
        }

        //------------------------------------------------------------------------------------------
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
