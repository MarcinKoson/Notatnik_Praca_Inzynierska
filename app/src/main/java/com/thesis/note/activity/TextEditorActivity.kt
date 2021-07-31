package com.thesis.note.activity

import android.content.Intent
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
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityTextEditorBinding


class TextEditorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityTextEditorBinding

//todo do sth with exitstInDB booleans
    var dataExistInDB:Boolean = false
    var noteExistInDB:Boolean = false
    var dataID:Int = -1
    var noteID:Int = -1

    val TextEditorActivityContext = this

    lateinit var editedData: Data

    val db = AppDatabase.invoke(this)

    lateinit var noteViewerActivityIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorBinding.inflate(layoutInflater) //LAYOUT BINDING CLASS
        setContentView(binding.root)
        drawer_layout = binding.activityTextEditorLayout
        navigationDrawer = NavigationDrawer(drawer_layout)
        binding.navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
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
                            binding.textField.text = Editable.Factory.getInstance().newEditable(editedData.Content)
                        })}}
            //check if new note
            noteExistInDB = noteID == -1
                }
        else{
            dataExistInDB = false
            noteExistInDB = false
        }

        //SAVE button
        binding.saveButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(dataExistInDB){
                    //Update data
                    editedData.Content = binding.textField.text.toString()
                    GlobalScope.launch {
                        db.dataDao().update(editedData)
                    }
                }
                else{
                    if(noteExistInDB){
                    //Add new data to database
                    GlobalScope.launch {
                        val newDataID = db.dataDao().insertAll(Data(0,noteID,NoteType.Text,binding.textField.text.toString(),null,null,null))
                        dataID = newDataID[0].toInt()
                        dataExistInDB = true
                    }}
                    else{
                        GlobalScope.launch {
                            //add new note
                            var idNewNote = db.noteDao().insertAll(Note(0,"",null,null,false,null,null,null,null))
                            noteID = idNewNote[0].toInt()
                            //add new data
                            val newDataID = db.dataDao().insertAll(Data(0,noteID,NoteType.Text,binding.textField.text.toString(),null,null,null))
                            dataID = newDataID[0].toInt()
                            val note = db.noteDao().getNoteById(noteID)
                            note.MainData = dataID
                            db.noteDao().update(note)
                            //open new note
                            noteViewerActivityIntent.putExtra("noteID",noteID)
                            TextEditorActivityContext.startActivity(noteViewerActivityIntent)
                        }




                    }
                }
                Toast.makeText(applicationContext,"ZAPISANO", Toast.LENGTH_SHORT).show()
                finish()
            }})

        //REMOVE button
        binding.deleteButton.setOnClickListener(object : View.OnClickListener{
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
