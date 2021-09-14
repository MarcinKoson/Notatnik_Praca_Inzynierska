package com.thesis.note.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.database.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityTextEditorLayoutBinding
import com.thesis.note.fragment.ColorPickerFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

//TODO speech to text
/**
 * Activity for text editing.
 *
 * When creating [Intent] of this activity, you should put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * If passed id equals "-1" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "-1".
 */
class TextEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityTextEditorLayoutBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Data] id */
    private var dataID:Int = -1
    /** Edited [Data] */
    private lateinit var editedData: Data
    /** Edited [Note]  id */
    private var noteID:Int = -1
    /** Edited [Note] */
    private lateinit var editedNote: Note

    /** */
    private var italic = false
    /** */
    private var bold = false
    /** */
    private var fontSize = 16
    /** */
    private var fontColor = NoteColor.Black

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorLayoutBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase.invoke(this)
        loadParameters()
        GlobalScope.launch {
            loadData()
            runOnUiThread {
                setData()
            }
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
            if (dataID != -1) {
                //Update data
                GlobalScope.launch {
                    db.dataDao().update(editedData.apply{
                        Content = binding.editedText.text.toString()
                        Info = getInfo()
                        Color = fontColor
                        Size = fontSize
                    })
                    db.noteDao().update(editedNote.apply { Date = Date()})
                }
            } else {
                if (noteID != -1) {
                    //Add new data to database
                    GlobalScope.launch {
                        db.dataDao().insertAll(Data(0, noteID, NoteType.Text, binding.editedText.text.toString(), getInfo(),fontSize,fontColor))
                        db.noteDao().update(editedNote.apply { Date = Date()})
                    }
                } else {
                    GlobalScope.launch {
                        //add new note
                        val idNewNote =
                            db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White))
                        noteID = idNewNote[0].toInt()
                        //add new data
                        val newDataID = db.dataDao().insertAll(Data(0, noteID, NoteType.Text, binding.editedText.text.toString(), getInfo(),fontSize,fontColor))
                        dataID = newDataID[0].toInt()
                        editedNote = db.noteDao().getNoteById(noteID)
                        db.noteDao().update(editedNote.apply { MainData = dataID })
                        //open new note
                        runOnUiThread {
                            thisActivity.startActivity(Intent(thisActivity, NoteViewerActivity::class.java).apply{putExtra("noteID", noteID)})
                        }
                    }
                }
            }
            Toast.makeText(applicationContext, R.string.activity_text_editor_save_OK, Toast.LENGTH_SHORT).show()
            finish()
        }

        //TODO Delete button listener
        binding.deleteButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //TODO Share button listener
        binding.shareButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //Text color button listener
        binding.textColorButton.setOnClickListener {
            ColorPickerFragment(ColorPalette.TEXT_COLOR_PALETTE).show(supportFragmentManager, "tag")
        }

        //ColorPickerFragment result listener
        supportFragmentManager.setFragmentResultListener("color",this) { _, bundle ->
            val result = bundle.getInt("colorID")
            fontColor = NoteColorConverter().intToEnum(result)!!
            binding.editedText.setTextColor(resources.getColor(NoteColorConverter.enumToColor(fontColor),null))
        }

        //TODO Text size button listener
        binding.textSizeButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //Italic button listener
        binding.italicTextButton.setOnClickListener{
            if(italic){
                setItalicText(false)
            }
            else{
                setItalicText(true)
            }
        }

        //Bold button listener
        binding.boldTextButton.setOnClickListener {
            if(bold){
                setBoldText(false)
            }
            else{
                setBoldText(true)
            }
        }

    }

    /** Load parameters passed from another activity */
    private fun loadParameters() {
        val parameters = intent.extras
        if (parameters == null) {
            Toast.makeText(
                applicationContext,
                R.string.activity_text_editor_error_parameters,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
        }
    }

    /** Load [Note] with id [noteID] and [Data] with id [dataID]  */
    private fun loadData(){
        if(dataID != -1){
            editedData = db.dataDao().getDataById(dataID)
            fontSize = editedData.Size!!
            fontColor = editedData.Color!!
            when(editedData.Info){
                "B" -> bold = true
                "I" -> italic = true
                "BI" -> {
                    bold = true
                    italic = true}
            }
        }
        if(noteID != -1){
            editedNote = db.noteDao().getNoteById(noteID)
        }
    }

    /** Set loaded [Data] into layout */
    private fun setData(){
        if(dataID != -1) {
            //show data in textField
            setText(editedData.Content)
            //load graphic options
            setItalicText(italic)
            if (italic) {
                binding.italicTextButton.isChecked = true
            }
            setBoldText(bold)
            if (bold) {
                binding.boldTextButton.isChecked = true
            }
            binding.editedText.textSize = fontSize.toFloat()
            binding.editedText.setTextColor(
                resources.getColor(
                    NoteColorConverter.enumToColor(
                        fontColor
                    ), null
                )
            )
            //set background
            binding.editedText.background = ResourcesCompat.getDrawable(
                resources,
                NoteColorConverter.enumToColor(editedNote.Color),
                null
            )
        }
    }

    /** Get info about italic and bold */
    private fun getInfo(): String? {
        if(bold && italic)
            return "BI"
        if(bold)
            return "B"
        if(italic)
            return "I"
        return null
    }

    /** Set italic text when [value] is true, disable italic text when [value] is false */
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

    /** Set bold text when [value] is true, disable bold text when [value] is false */
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

    /** Set text to bold and italic */
    private fun setBoldItalicText() {
        binding.editedText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }

    /** Set content into text edit */
    private fun setText(content: String?){
        binding.editedText.text = Editable.Factory.getInstance().newEditable(content)
    }

    /** Logic for back button */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_text_editor_discard_changes_positive) { _, _ ->
                    GlobalScope.launch {
                        runOnUiThread {
                            super.onBackPressed()
                        }
                    }
                }
                setNegativeButton(R.string.activity_text_editor_discard_changes_negative) { _, _ -> }
                setTitle(R.string.activity_text_editor_discard_changes)
                create()
            }.show()
        }
    }
}
