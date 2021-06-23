package com.thesis.note.activity

import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View

import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer


import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType

import com.google.android.material.navigation.NavigationView



import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import com.thesis.note.R
import com.thesis.note.database.entity.Data
import kotlinx.android.synthetic.main.activity_text_editor_new.*

class TextEditorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

    var dataExistInDB:Boolean = false
    var dataID:Int = -1
    var noteID:Int = -1

    val TextEditorActivityContext = this

    lateinit var editedData: Data

    val db = AppDatabase.invoke(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_editor_new)
        drawer_layout = activity_text_editor_new_layout
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------

        //Check if new data or edit of existing
        val parameters = intent.extras

        if(parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")

            if(dataID <= 0){
                dataExistInDB = false
            }
            else{
                //load data
                GlobalScope.launch {
                    editedData = db.dataDao().getDataById(dataID)
                    dataExistInDB = true
                    //show data in textField
                    TextEditorActivityContext.runOnUiThread(
                        fun(){
                            textField.text = Editable.Factory.getInstance().newEditable(editedData.Content)
                        })}}}
        else{
            dataExistInDB = false
        }

        //SAVE button
        saveButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(dataExistInDB){
                    //Update data
                    editedData.Content = textField.text.toString()
                    GlobalScope.launch {
                        db.dataDao().updateTodo(editedData)
                    }
                }
                else{
                    //Add new data to database
                    GlobalScope.launch {
                        val newDataID = db.dataDao().insertAll(Data(0,noteID,NoteType.Text,textField.text.toString(),null))
                        dataID = newDataID[0].toInt()
                        dataExistInDB = true
                    }
                }
                Toast.makeText(applicationContext,"ZAPISANO", Toast.LENGTH_SHORT).show()
            }})

        //REMOVE button
        deleteButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(dataExistInDB)
                GlobalScope.launch {
                    val db = AppDatabase(applicationContext)
                    db.dataDao().delete(editedData)
                }
                Toast.makeText(applicationContext,"USUNIĘTO", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
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
