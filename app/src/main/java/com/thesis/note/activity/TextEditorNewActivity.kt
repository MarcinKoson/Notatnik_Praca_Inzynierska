package com.thesis.note.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.thesis.note.NavigationDrawer
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityTextEditorNewLayoutBinding
import com.thesis.note.fragment.ChooseColorFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TextEditorNewActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    lateinit var binding: ActivityTextEditorNewLayoutBinding

    private val textEditorActivityContext = this

    private lateinit var db: AppDatabase
    private var dataID:Int = -1
    private var noteID:Int = -1
    private lateinit var editedData: Data

    private lateinit var noteViewerActivityIntent : Intent

    private var italic = false
    private var bold = false
    private var fontSize = 10
    private var fontColor = NoteColor.Black


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorNewLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityTextEditorNewLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle = ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        //load from db
        db = AppDatabase.invoke(this)
        val parameters = intent.extras
        if(parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
            //load data
            if(dataID != -1){
                GlobalScope.launch {
                    editedData = db.dataDao().getDataById(dataID)
                    //show data in textField
                    textEditorActivityContext.runOnUiThread {
                        setText(editedData.Content)
                        //TODO load graphic options
                    }
                }}
        }
        //bold text
        binding.boldTextButton.setOnClickListener {
            if(bold){
                setBoldText(false)
            }
            else{
                setBoldText(true)
            }
        }
        //italic text
        binding.italicTextButton.setOnClickListener{
            if(italic){
                setItalicText(false)
            }
            else{
                setItalicText(true)
            }
        }
        //text size
        binding.textSizeButton.setOnClickListener {
            //TODO font size
        }
        //text color
        binding.textColorButton.setOnClickListener {
            ChooseColorFragment().show(supportFragmentManager,"tag")
        }
        supportFragmentManager.setFragmentResultListener("color",this) { key, bundle ->
            val result = bundle.getString("colorID")
            val colorID = result?.toInt()
            fontColor = NoteColorConverter().intToEnum(colorID)!!
            binding.editedText.setTextColor(NoteColorConverter().intToColor(colorID,resources))
        }
        //save button
        //TODO save new stuff
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
                                NoteType.Text, binding.editedText.text.toString(), null,10,NoteColor.White //TODO size color
                            )
                        )
                        dataID = newDataID[0].toInt()
                    }
                } else {
                    GlobalScope.launch {
                        //add new note
                        val idNewNote =
                            db.noteDao().insertAll(Note(0, "", null, null, false, null, null, null, NoteColor.White))//TODO color
                        noteID = idNewNote[0].toInt()
                        //add new data
                        val newDataID = db.dataDao().insertAll(
                            Data(
                                0, noteID,
                                NoteType.Text, binding.editedText.text.toString(), null,10, NoteColor.Black  //TODO size
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

    }

    private fun setItalicText(value: Boolean) {
        italic = value
        if(value){
            if(bold){
                setBoldItalicText()
            }else{
                binding.editedText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
        }
        else{
            if(bold){
                setBoldText(true)
            }else{
                binding.editedText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
        }
    }

    private fun setBoldText(value: Boolean) {
        bold = value
        if(value){
            if(italic){
                setBoldItalicText()
            }else{
                binding.editedText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        }
        else{
            if(italic){
                setItalicText(true)
            }else{
                binding.editedText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
        }
    }

    private fun setBoldItalicText() {
        binding.editedText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
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

    private fun setText(content: String?){
        binding.editedText.text = Editable.Factory.getInstance().newEditable(content)
    }
}
