package com.thesis.note.activity

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

import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Group
import com.thesis.note.databinding.ActivitySearchBinding

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//TODO
class SearchActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivitySearchBinding

    lateinit var groupsList: List<Group>
    val db = AppDatabase.invoke(this)
    val SearchActivityContext = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawer_layout = binding.activitySearchLayout           //NAZWA DRAWER LAYOUTU
        navigationDrawer = NavigationDrawer(drawer_layout)
        binding.navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()

        binding.searchButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var sv = SearchValues()
                sv.name = binding.editText.text.toString()

                SearchValuesS.name = "%"+binding.editText.text.toString()+"%"
                SearchValuesS.favorite = binding.checkBox.isChecked

                if(binding.groupSpinner2.selectedItemPosition!=0)
                    SearchValuesS.group = groupsList[binding.groupSpinner2.selectedItemPosition-1].IdGroup
                else
                    SearchValuesS.group = null

                finish()
            }
        })

        val spinner: Spinner = binding.groupSpinner2

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
