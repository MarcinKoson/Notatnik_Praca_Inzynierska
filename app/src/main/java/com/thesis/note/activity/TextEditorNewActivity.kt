package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityTextEditorNewLayoutBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TextEditorNewActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityTextEditorNewLayoutBinding

    private val textEditorActivityContext = this

    private lateinit var db: AppDatabase
    var dataID:Int = -1
    var noteID:Int = -1
    private lateinit var editedData: Data

    lateinit var noteViewerActivityIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorNewLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityTextEditorLayoutLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle = ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase.invoke(this)

        val parameters = intent.extras

        if(parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")

            if(dataID != -1){
                GlobalScope.launch {
                    editedData = db.dataDao().getDataById(dataID)
                    //show data in textField
                    textEditorActivityContext.runOnUiThread(
                        fun(){
                            setText()
                        })}}
        }

        noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
        binding.saveButton.setOnClickListener {
            if (dataID != -1) {
                //Update data
                editedData.Content = binding.editedText.text.toString()
                GlobalScope.launch {
                    db.dataDao().update(editedData)
                }
            } else {
                if (noteID != -1) {
                    //Add new data to database
                    GlobalScope.launch {
                        val newDataID = db.dataDao().insertAll(
                            Data(
                                0, noteID,
                                NoteType.Text, binding.editedText.text.toString(), null
                            )
                        )
                        dataID = newDataID[0].toInt()
                    }
                } else {
                    GlobalScope.launch {
                        //add new note
                        val idNewNote =
                            db.noteDao().insertAll(Note(0, "", null, null, false, null, null, null))
                        noteID = idNewNote[0].toInt()
                        //add new data
                        val newDataID = db.dataDao().insertAll(
                            Data(
                                0, noteID,
                                NoteType.Text, binding.editedText.text.toString(), null
                            )
                        )
                        dataID = newDataID[0].toInt()
                        val note = db.noteDao().getNoteById(noteID)
                        note.MainData = dataID
                        db.noteDao().update(note)
                        //open new note
                        noteViewerActivityIntent.putExtra("noteID", noteID)
                        textEditorActivityContext.startActivity(noteViewerActivityIntent)
                    }
                }
            }
            Toast.makeText(applicationContext, R.string.save_OK, Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.shareButton.setOnClickListener {
            //TODO share
        }
        binding.backgroundButton.setOnClickListener {
            //TODO background
        }
        binding.textColorButton.setOnClickListener {
            //TODO font color
        }
        binding.textSizeButton.setOnClickListener {
            //TODO font size
        }
        binding.underlinedTextButton.setOnClickListener {
            //TODO underline
        }
        binding.italicTextButton.setOnClickListener{
            //TODO italic
            setItalicText()
        }
        binding.boldTextButton.setOnClickListener {
            //TODO bold
        }

    }

    private fun setItalicText() {
        TODO("Not yet implemented")
    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setText(){
        binding.editedText.text = Editable.Factory.getInstance().newEditable(editedData.Content)
    }
}
