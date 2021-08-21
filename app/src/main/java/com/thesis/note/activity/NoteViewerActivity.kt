package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.*
import com.thesis.note.database.*
import com.thesis.note.database.entity.*
import com.thesis.note.databinding.ActivityNoteViewerBinding
import com.thesis.note.fragment.AddTagsDialogFragment
import com.thesis.note.fragment.ColorPickerFragment
import com.thesis.note.recycler_view_adapters.NoteViewerAdapter
import com.thesis.note.recycler_view_adapters.TagListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *
 * Activity for viewing note.
 *
 * When creating [Intent] of this activity, you should put extended data with
 * putExtra("noteID", yourNoteID)
 *
 * TODO tags saving
 */
class NoteViewerActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityNoteViewerBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Viewed [Note] id */
    var noteID: Int = -1
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

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteViewerBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root, binding.toolbar, binding.navigationView)
        //------------------------------------------------------------------------------------------
        db = AppDatabase(this)
        loadParameters()
        if(noteID != -1) {
            GlobalScope.launch {
                loadNote()
                setNote()
            }
        }
        //Save button listener
        binding.saveButton.setOnClickListener {
            GlobalScope.launch {
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
                //color saved in color change fragment listener
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
                        db.noteDao().delete(note)
                        thisActivity.finish()
                    }
                }
                setNegativeButton(R.string.activity_note_viewer_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_note_viewer_dialog_remove_note)
                create()
            }.show()
        }

        //TODO Share button listener
        binding.shareButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
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
            note.Color = NoteColorConverter().intToEnum(result)!!
            binding.root.background = ResourcesCompat.getDrawable(
                resources,
                NoteColorConverter.enumToColor(note.Color),
                null
            )
        }

        //Background color button listener
        binding.backgroundColorButton.setOnClickListener {
            ColorPickerFragment(ColorPalette.NOTE_BACKGROUND_PALETTE).show(supportFragmentManager, "tag")
        }
    }

    /** On resume callback */
    override fun onResume() {
        super.onResume()
        //Update data
        if(noteID != -1){
            GlobalScope.launch {
                //load data form db
                dataList = db.dataDao().getDataFromNote(noteID)
                //set new data to recycler view
                runOnUiThread {
                    val viewAdapter = NoteViewerAdapter(dataList, onDataClickListener)
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
                NoteType.Photo.id -> {
                    Intent(thisActivity, ImageNoteActivity::class.java).run{
                        putExtra("noteID", noteID)
                        putExtra("dataID", dataList[position].IdData)
                        startActivity(this)
                    }
                }
                NoteType.Sound.id -> {
                    Intent(thisActivity, SoundEditorActivity::class.java).run{
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
    private  fun setNote(){
        //Set note name
        binding.noteName.text = Editable.Factory.getInstance().newEditable(note.Name)
        //Set date
        binding.noteDate.text = DateConverter().dateToString(note.Date)
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
        val viewAdapter = NoteViewerAdapter(dataList, onDataClickListener)
        binding.noteViewerRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        //Set background color
        binding.root.background = ResourcesCompat.getDrawable(
            resources,
            NoteColorConverter.enumToColor(note.Color),
            null
        )
    }

    /** Logic for back button */
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_note_viewer_discard_changes_positive) { _, _ ->
                    GlobalScope.launch {
                        runOnUiThread {
                            super.onBackPressed()
                        }
                    }
                }
                setNegativeButton(R.string.activity_note_viewer_discard_changes_negative) { _, _ -> }
                setTitle(R.string.activity_note_viewer_discard_changes)
                create()
            }.show()
        }
    }
}
