package com.thesis.note.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import com.thesis.note.R
import com.thesis.note.database.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityTextEditorLayoutBinding
import com.thesis.note.fragment.ColorPickerFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.thesis.note.Constants

/**
 * Activity for text editing.
 *
 * When creating [Intent] of this activity, you can put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * Activity will load [Note] and [Data] with passed id.
 * If passed id equals "0" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "0".
 *
 */
class TextEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityTextEditorLayoutBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Note] id */
    private var noteID:Int = 0

    /** Edited [Note] */
    private var editedNote: Note? = null

    /** Edited [Data] id */
    private var dataID:Int = 0

    /** Edited [Data] */
    private var editedData: Data? = null

    /** Is current text italic */
    private var italic = false

    /** Is current text bold */
    private var bold = false

    /** Current font size */
    private var fontSize : Int = Constants.TEXT_SIZE_SMALL.toInt()

    /** Current font color */
    private var fontColor = NoteColor.Black

    /** List of size of font */
    val fontSizeList = listOf(8,12,16,21,25,27,30)

    /** On create callback. Loading data, layout init and setting listeners */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorLayoutBinding.inflate(layoutInflater)
        loadSettings()
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase.invoke(this)
        loadParameters()
        GlobalScope.launch {
            loadFromDB()
            runOnUiThread {
                setLayout()
            }
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
            when {
                dataID != 0 -> {
                    //update
                    GlobalScope.launch {
                        editedData?.apply{
                            Content = binding.editedText.text.toString()
                            Info = getInfo()
                            Color = fontColor
                            Size = fontSize
                        }?.let { it1 -> db.dataDao().update(it1) }
                        editedNote?.apply { Date = Date()}?.let { it1 -> db.noteDao().update(it1) }
                    }
                }
                noteID != 0 -> {
                    //add new data to db
                    GlobalScope.launch {
                        val addedData = db.dataDao().insertAll(Data(0, noteID, NoteType.Text, binding.editedText.text.toString(), getInfo(),fontSize,fontColor))
                        editedNote?.apply { Date = Date(); if(MainData==null) MainData=addedData[0].toInt()}?.let { it1 ->
                            db.noteDao().update(it1)
                        }
                    }
                }
                else -> {
                    //add new note
                    GlobalScope.launch {
                        //add new note
                        db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White)).also {
                            noteID = it[0].toInt()
                        }
                        //add new data
                        db.dataDao().insertAll(Data(0, noteID, NoteType.Text, binding.editedText.text.toString(), getInfo(),fontSize,fontColor)).also {
                            dataID = it[0].toInt()
                            db.noteDao().update(db.noteDao().getNoteById(noteID).apply{ MainData = dataID })
                        }
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

        //Delete button listener
        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_text_editor_dialog_remove_note_positive_button) { _, _ ->
                    if (dataID != 0){
                        GlobalScope.launch {
                            if (editedNote?.MainData == dataID){
                                with(db.dataDao().getDataFromNote(noteID).map { it.IdData }.toMutableList()){
                                    remove(dataID)
                                    if(size == 0)
                                        editedNote!!.MainData = null
                                    else
                                        editedNote!!.MainData = this[0]
                                    db.noteDao().update(editedNote!!)
                                }
                            }
                            editedData?.let { it1 -> db.dataDao().delete(it1) }
                        }
                    }
                    finish()
                }
                setNegativeButton(R.string.activity_text_editor_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_text_editor_dialog_remove_note)
                create()
            }.show()
        }

        //Share button listener
        binding.shareButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply{
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, editedData?.Content)
                startActivity(Intent.createChooser(this, getString(R.string.activity_text_editor_share)))
                //startActivity(this)
            }
        }

        //Speech to text button listener
        binding.micButton.setOnClickListener {
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                try {
                    startForResultSpeechToText.launch(this)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(thisActivity, R.string.activity_text_editor_stt_error, Toast.LENGTH_SHORT).show()
                }
            }
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

        //Text size spinner setup
        binding.textSizeSpinner.apply {
            adapter = ArrayAdapter(thisActivity, android.R.layout.simple_spinner_item, fontSizeList).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            fontSizeList.indexOf(fontSize).let { if(it!=-1) setSelection(it) }
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                    fontSize = fontSizeList[position]
                    binding.editedText.textSize = fontSizeList[position].toFloat()
                }
                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }
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
        if (parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
        }
    }

    /** Load [Data] and [Note] form database. Load [bold], [italic], [fontColor], [fontSize] from [Data] */
    private fun loadFromDB() {
        if (noteID != 0) {
            editedNote = db.noteDao().getNoteById(noteID)
        }
        if (dataID != 0) {
            editedData = db.dataDao().getDataById(dataID)
            editedData?.let {
                it.Size?.let {size -> fontSize = size}
                it.Color?.let { color -> fontColor = color}
                when(it.Info){
                    "B" -> bold = true
                    "I" -> italic = true
                    "BI" -> {
                        bold = true
                        italic = true}
                }
            }
            if (noteID == 0) {
                noteID = editedData?.NoteId?:0
                editedNote = db.noteDao().getNoteById(noteID)
            }
        }
    }

    /** Set loaded [Data] and [Note] into layout */
    private fun setLayout(){
        if(dataID != 0) {
            //show data in textField
            editedData?.Content?.let { setText(it) }
            //set italic text
            setItalicText(italic)
            if (italic) {
                binding.italicTextButton.isChecked = true
            }
            //set bold text
            setBoldText(bold)
            if (bold) {
                binding.boldTextButton.isChecked = true
            }
            //set font size
            binding.editedText.textSize = fontSize.toFloat()
            fontSizeList.indexOf(fontSize).let { if(it!=-1 && binding.textSizeSpinner.adapter != null) binding.textSizeSpinner.setSelection(it) }
            //set font color
            binding.editedText.setTextColor(
                resources.getColor(
                    NoteColorConverter.enumToColor(
                        fontColor
                    ), null
                )
            )
            //TODO set background
            editedNote?.Color?.let {
                binding.root.background =
                    ResourcesCompat.getDrawable(resources, NoteColorConverter.enumToColor(it), null)
            }
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
    private fun setText(content: String){
        binding.editedText.text = Editable.Factory.getInstance().newEditable(content)
    }

    /** Callback from speech to text */
    private val startForResultSpeechToText = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultArray = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = resultArray?.get(0)
            binding.editedText.text = binding.editedText.text?.append(" $recognizedText")
        }
    }

    /** Logic for back button. Show discard changes dialog */
    override fun onBackPressed() {
        if (binding.root.isDrawerOpen(GravityCompat.START)) {
            binding.root.closeDrawer(GravityCompat.START)
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

    /** Load settings related to this activity */
    private fun loadSettings(){
        binding.deleteButton.also { item ->
            with(sharedPreferences.getBoolean("text_editor_delete", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.shareButton.also { item ->
            with(sharedPreferences.getBoolean("text_editor_share", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.micButton.also { item ->
            with(sharedPreferences.getBoolean("text_editor_speech_to_text", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.textColorButton.also { item ->
            with(sharedPreferences.getBoolean("text_editor_color", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.textSizeSpinner.also { item ->
            with(sharedPreferences.getBoolean("text_editor_size", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.italicTextButton.also { item ->
            with(sharedPreferences.getBoolean("text_editor_italic", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.boldTextButton.also { item ->
            with(sharedPreferences.getBoolean("text_editor_bold", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
    }

}
