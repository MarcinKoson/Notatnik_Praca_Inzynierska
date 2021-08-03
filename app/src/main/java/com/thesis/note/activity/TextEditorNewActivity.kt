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
    private lateinit var binding: ActivityTextEditorNewLayoutBinding

    private val textEditorActivityContext = this

    private lateinit var db: AppDatabase
    var dataID:Int = -1
    var noteID:Int = -1
    private lateinit var editedData: Data

    lateinit var noteViewerActivityIntent : Intent

    private var italic = false
    var bold = false
    var fontSize = 10
    var fontColor = NoteColor.Black





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

        binding.shareButton.setOnClickListener {
            //TODO share
        }


        supportFragmentManager.setFragmentResultListener("requestKey",this) { key, bundle ->
            val result = bundle.getString("bundleKey")
binding.editedText.text = Editable.Factory.getInstance().newEditable(result)


            when(result){
                "1" ->   binding.editedText.setTextColor(resources.getColor(R.color.blue_400,null))
                "2" ->   binding.editedText.setTextColor(resources.getColor(R.color.green_400,null))
                "3" ->   binding.editedText.setTextColor(resources.getColor(R.color.red_400,null))
                //    "3"->   binding.editedText.background = ContextCompat.getDrawable(textEditorActivityContext, R.color.red_400)
            }

        }


        binding.textColorButton.setOnClickListener {
            val newFragment = ChooseColorFragment()
            newFragment.show(supportFragmentManager,"tag")


        }
        binding.textSizeButton.setOnClickListener {
            //TODO font size
        }
        binding.underlinedTextButton.setOnClickListener {
            //TODO underline
        }
        binding.italicTextButton.setOnClickListener{
            if(italic){
                italic = false
                setItalicText(false)

            }
            else{
                italic = true
                setItalicText(true)


            }
        }
        binding.boldTextButton.setOnClickListener {
            if(bold){
                bold = false
                setBoldText(false)

            }
            else{
                bold = true
                setBoldText(true)

            }
        }

    }

    private fun setItalicText(value: Boolean) {
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

    private fun setText(){
        binding.editedText.text = Editable.Factory.getInstance().newEditable(editedData.Content)
    }
}
