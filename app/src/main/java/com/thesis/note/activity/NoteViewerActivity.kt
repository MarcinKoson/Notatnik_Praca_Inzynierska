package com.thesis.note.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.R
import com.thesis.note.database.*
import com.thesis.note.database.entity.*
import com.thesis.note.databinding.ActivityNoteViewerBinding
import com.thesis.note.fragment.AddNoteFragment
import com.thesis.note.fragment.AddTagsDialogFragment
import com.thesis.note.fragment.ColorPickerFragment
import com.thesis.note.recycler_view_adapters.ListViewerAdapter
import com.thesis.note.recycler_view_adapters.NoteViewerAdapter
import com.thesis.note.recycler_view_adapters.TagListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Activity for viewing note.
 *
 * When creating [Intent] of this activity, you should put extended data with
 * putExtra("noteID", yourNoteID)
 *
 */
class NoteViewerActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityNoteViewerBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Viewed [Note] id */
    var noteID: Int = 0

    /** Viewed [Note] */
    private lateinit var note: Note

    /** List of [Data] in [Note] */
    private lateinit var dataList: List<Data>

    /** List of all [Group] */
    private lateinit var groupsList: List<Group>

    /** List of all [Tag] */
    private lateinit var tagsList: List<Tag>

    /** List of [TagOfNote] */
    private lateinit var tagsOfNoteList: List<TagOfNote>

    /** Current background color */
    private var backgroundColor: Color? = null

    /** On create callback. Layout init and setting listeners */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteViewerBinding.inflate(layoutInflater)
        loadSettings()
        setDrawerLayout(binding.root, binding.toolbar, binding.navigationView)
        //------------------------------------------------------------------------------------------
        db = AppDatabase(this)
        loadParameters()
        if(noteID != 0) {
            GlobalScope.launch {
                loadNote()
                setNote()
            }
        }
        //Save button listener
        binding.saveButton.setOnClickListener {
            GlobalScope.launch {
                //Update NoteType.List
                updateListNotes()
                //Save note name
                note.Name = binding.noteName.text.toString()
                //Save group
                val spinnerPosition = binding.groupSpinner.selectedItemId.toInt()
                if (spinnerPosition == 0) {
                    note.GroupID = null
                } else {
                    //on first position in spinner is no group
                    note.GroupID = groupsList[spinnerPosition - 1].IdGroup
                }
                //update date
                note.Date = Date()
                //color
                note.Color = backgroundColor
                //favorite
                note.Favorite = binding.favoriteCheckBox.isChecked
                //Update
                db.noteDao().update(note)
                //Close activity
                runOnUiThread{
                    Toast.makeText(thisActivity, R.string.activity_note_viewer_save_OK, Toast.LENGTH_SHORT).show()
                }
                thisActivity.finish()
            }
        }

        //Remove button listener
        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_note_viewer_dialog_remove_note_positive_button) { _, _ ->
                    GlobalScope.launch {
                        val db = AppDatabase(applicationContext)
                        dataList.forEach {
                            if(it.Type == NoteType.Recording || it.Type==NoteType.Image){
                                try{ File(it.Content).delete() }catch(ex:Exception){}
                            }
                        }
                        db.noteDao().delete(note)
                        thisActivity.finish()
                    }
                }
                setNegativeButton(R.string.activity_note_viewer_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_note_viewer_dialog_remove_note)
                create()
            }.show()
        }

        //Share button listener
        binding.shareButton.setOnClickListener {
            val mainData = dataList.find { it.IdData == note.MainData }
            when{
                mainData == null -> {}
                mainData.Type == NoteType.Text -> {
                    Intent(Intent.ACTION_SEND).apply{
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, mainData.Content)
                        startActivity(Intent.createChooser(this, getString(R.string.activity_text_editor_share)))
                        //startActivity(this)
                    }
                }
                mainData.Type == NoteType.List -> {
                    var noteContent = ""
                    val listData = ListData().apply { loadData(mainData) }
                    listData.itemsList.forEach {
                        if(!it.checked){
                            noteContent += "•" + it.text + "\r\n"
                        }
                    }
                    Intent(Intent.ACTION_SEND).apply{
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, noteContent)
                        startActivity(Intent.createChooser(this, getString(R.string.activity_text_editor_share)))
                        //startActivity(this)
                    }
                }
                mainData.Type == NoteType.Image -> {
                    Intent(Intent.ACTION_SEND).apply{
                        type = "image/jpg"
                        putExtra(Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                thisActivity,
                                "com.thesis.note.fileprovider",
                                File(mainData.Content)
                            )
                        )
                        startActivity(Intent.createChooser(this, getString(R.string.activity_image_note_share)))
                        //startActivity(this)
                    }
                }
                mainData.Type == NoteType.Recording -> {
                    Intent(Intent.ACTION_SEND).apply{
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                thisActivity,
                                "com.thesis.note.fileprovider",
                                File(mainData.Content)
                            )
                        )
                        startActivity(Intent.createChooser(this, getString(R.string.activity_image_note_share)))
                    //startActivity(this)
                    }
                }
                else -> {}
            }
        }

        //AddTagsDialogFragment result listener
        supportFragmentManager.setFragmentResultListener("addedTag",this){ _, _ ->
            GlobalScope.launch {
                updateTagRecyclerView()
            }
        }

        //Add tag button listener
        binding.tagButton.setOnClickListener {
           AddTagsDialogFragment().run{
               arguments = Bundle().apply {
                   putInt("noteID", noteID)
               }
               show(supportFragmentManager, "add tags")
           }
        }

        //Color picker fragment listener
        supportFragmentManager.setFragmentResultListener("color", this) { _, bundle ->
            val result = bundle.getInt("colorID")
            showDiscardChangesDialog = true
            backgroundColor = ColorConverter().intToEnum(result)
            binding.root.background = ResourcesCompat.getDrawable(
                resources,
                ColorConverter.enumToColor(backgroundColor),
                null
            )
        }

        //Background color button listener
        binding.backgroundColorButton.setOnClickListener {
            ColorPickerFragment(ColorPalette.NOTE_BACKGROUND_PALETTE).show(supportFragmentManager, "tag")
        }

        //Add data button listener
        binding.addButton.setOnClickListener {
            AddNoteFragment(noteID).show(supportFragmentManager,"add_note")
        }

        //Favorite button listener
        binding.favoriteCheckBox.setOnClickListener {
            note.Favorite = binding.favoriteCheckBox.isChecked
            showDiscardChangesDialog = true
        }

    }

    /** On resume callback. Loads data from database. */
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        //Update data
        if(noteID != 0){
            GlobalScope.launch {
                note = db.noteDao().getNoteById(noteID)
                //load data form db
                dataList = db.dataDao().getDataFromNote(noteID)
                //set new data to recycler view
                runOnUiThread {
                    val viewAdapter = NoteViewerAdapter(dataList, {thisActivity.showDiscardChangesDialog = true}, onDataClickListener)
                    binding.noteViewerRecyclerView.adapter = viewAdapter
                    viewAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    /** Listener for click on data. Opens editor for clicked data. */
    private val onDataClickListener = object : NoteViewerAdapter.OnDataClickListener{
        override fun onDataClick(position: Int) {
            when (binding.noteViewerRecyclerView.adapter?.getItemViewType(position)) {
                NoteType.Text.id -> {
                    Intent(thisActivity, TextEditorActivity::class.java).run {
                        putExtra("noteID", noteID)
                        putExtra("dataID", dataList[position].IdData)
                        startActivity(this)
                    }
                }
                NoteType.List.id -> {
                    Intent(thisActivity, ListEditorActivity::class.java).run {
                        putExtra("noteID", noteID)
                        putExtra("dataID", dataList[position].IdData)
                        startActivity(this)
                    }
                }
                NoteType.Image.id -> {
                    Intent(thisActivity, ImageNoteActivity::class.java).run{
                        putExtra("noteID", noteID)
                        putExtra("dataID", dataList[position].IdData)
                        startActivity(this)
                    }
                }
                NoteType.Recording.id -> {
                    Intent(thisActivity, RecordingEditorActivity::class.java).run{
                        putExtra("noteID", noteID)
                        putExtra("dataID", dataList[position].IdData)
                        startActivity(this)
                    }
                }
                else -> {
                    Toast.makeText(applicationContext, R.string.activity_note_viewer_error_cannot_open, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Listener for click on tag. Opens remove dialog. */
    private val onTagClickListener = object : TagListAdapter.OnTagClickListener{
        override fun onTagClick(position: Int) {
            //create remove dialog
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_note_viewer_dialog_remove_tag_positive_button) { _, _ ->
                    GlobalScope.launch {
                        db.tagOfNoteDAO().delete(tagsOfNoteList[position])
                        updateTagRecyclerView()
                    }
                }
                setNegativeButton(R.string.activity_note_viewer_dialog_remove_tag_negative_button) { _, _ -> }
                setTitle(R.string.activity_note_viewer_dialog_remove_tag)
                create()
            }.show()
        }
    }

    /** Update tag recycler view. It load new list of [TagOfNote] from database and show it in recycler view */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateTagRecyclerView(){
        tagsOfNoteList = db.tagOfNoteDAO().getAllNoteTags(note.IdNote)
        runOnUiThread {
            val filteredTags = tagsList.filter { tag -> (tagsOfNoteList.any { tagOfNote -> tagOfNote.TagID == tag.IdTag }) }
            val viewAdapter = TagListAdapter(filteredTags,onTagClickListener)
            binding.tagRecyclerView.adapter = viewAdapter
            viewAdapter.notifyDataSetChanged()
        }
    }

    /** Load parameters passed from another activity */
    private fun loadParameters() {
        val parameters = intent.extras
        if (parameters == null) {
            Toast.makeText(
                applicationContext,
                R.string.activity_note_viewer_error_id_note,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            noteID = parameters.getInt("noteID")
        }
    }

    /** Load all data about note with id==[noteID] from database */
    private fun loadNote(){
        note = db.noteDao().getNoteById(noteID)
        dataList = db.dataDao().getDataFromNote(noteID)
        groupsList = db.groupDao().getAll()
        tagsList = db.tagDao().getAll()
        tagsOfNoteList = db.tagOfNoteDAO().getAllNoteTags(note.IdNote)
    }

    /** Set loaded note into layout */
    private fun setNote(){
        //Set note name
        binding.noteName.text = Editable.Factory.getInstance().newEditable(note.Name)
        //Set note name change listener
        binding.noteName.doOnTextChanged { _, _, _, _ -> showDiscardChangesDialog = true }
        //Set date
        binding.noteDate.text = DateConverter().dateToString(note.Date)
        //Set favorite
        binding.favoriteCheckBox.isChecked = note.Favorite
        //Set spinner of groups
        binding.groupSpinner.adapter = ArrayAdapter(
            thisActivity,
            android.R.layout.simple_spinner_item,
            groupsList.map { x -> x.Name }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            insert(getString(R.string.activity_note_viewer_without_group), 0)
        }
        //Load current group of note
        val group: Group? = groupsList.firstOrNull { x -> x.IdGroup == note.GroupID }
        if (group != null) {
            binding.groupSpinner.setSelection(groupsList.indexOf(group) + 1)
        }
        //Set listener for group change
        binding.groupSpinner.post {
            binding.groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                    showDiscardChangesDialog = true
                }
                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }
        }
        //Init tag RecyclerView
        val tagsViewManager = FlexboxLayoutManager(thisActivity)
        val filteredTags = tagsList.filter { tag -> (tagsOfNoteList.any { tagOfNote -> tagOfNote.TagID == tag.IdTag }) }
        val tagsViewAdapter = TagListAdapter(filteredTags,onTagClickListener)
        binding.tagRecyclerView.apply {
            layoutManager = tagsViewManager
            adapter = tagsViewAdapter
        }
        //Init data RecyclerView
        val viewManager = LinearLayoutManager(thisActivity)
        val viewAdapter = NoteViewerAdapter(dataList, {thisActivity.showDiscardChangesDialog = true} , onDataClickListener)
        binding.noteViewerRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        //Set background color
        backgroundColor = note.Color
        binding.root.background = ResourcesCompat.getDrawable(
            resources,
            ColorConverter.enumToColor(note.Color),
            null
        )
    }

    /** Update list notes in database*/
    private fun updateListNotes(){
        dataList.forEachIndexed { index, data ->
            if(data.Type == NoteType.List){
                db.dataDao().update(
                    ((binding.noteViewerRecyclerView.adapter as NoteViewerAdapter)
                    .getRecyclerView(index)
                    ?.adapter as ListViewerAdapter)
                    .getListData()
                    .getData()
                )
            }
        }
    }

    /** Load settings related to this activity */
    private fun loadSettings(){
        binding.deleteButton.also { item ->
            with(sharedPreferences.getBoolean("note_viewer_delete", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.shareButton.also { item ->
            with(sharedPreferences.getBoolean("note_viewer_share", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.tagButton.also { item ->
            with(sharedPreferences.getBoolean("note_viewer_tag", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.backgroundColorButton.also { item ->
            with(sharedPreferences.getBoolean("note_viewer_background_color", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.addButton.also { item ->
            with(sharedPreferences.getBoolean("note_viewer_add_data", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.favoriteCheckBox.also { item ->
            with(sharedPreferences.getBoolean("note_viewer_favorite", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
    }

}
