package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.ListData
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityListEditorLayoutBinding
import com.thesis.note.recycler_view_adapters.ListEditorAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 *  Activity for list editing.
 *
 * When creating [Intent] of this activity, you can put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * Activity will load [Note] and [Data] with passed id.
 * If passed id equals "0" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "0".
 *
 */
class ListEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityListEditorLayoutBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Note] id */
    private var noteID : Int = 0

    /** Edited [Note] */
    private var editedNote : Note? = null

    /** Edited [Data] id */
    private var dataID : Int = 0

    /** Edited [ListData] */
    private var editedListData = ListData()

    /** On create callback. Loading data, layout init and setting listeners */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListEditorLayoutBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase.invoke(this)
        //add empty list item for new notes
        editedListData.itemsList.add(ListData.ListItem())
        loadParameters()
        GlobalScope.launch {
            loadFromDB()
            runOnUiThread {
                initRecyclerView()
                //Set background color
                editedNote?.Color?.let {
                    binding.root.background =
                        ResourcesCompat.getDrawable(resources, NoteColorConverter.enumToColor(it), null)
                }
            }
        }

        //Save button listener.
        binding.saveButton.setOnClickListener {
            when {
                dataID != 0 -> {
                    //update
                    GlobalScope.launch {
                        db.dataDao().update(editedListData.getData())
                        editedNote?.apply { Date = Date() }?.let { it1 -> db.noteDao().update(it1) }
                    }
                }
                noteID != 0 -> {
                    //add new data to db
                    GlobalScope.launch {
                        val addedData = db.dataDao().insertAll(editedListData.run { this.idData = 0; this.noteID = thisActivity.noteID; getData() })
                        editedNote?.apply { Date = Date(); if(MainData==null) MainData=addedData[0].toInt()}?.let { it1 ->
                            db.noteDao().update(it1)
                        }
                    }
                }
                else -> {
                    //add new note
                    GlobalScope.launch {
                        db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White)).also {
                            noteID = it[0].toInt()
                            editedListData.noteID = noteID
                        }
                        db.dataDao().insertAll(editedListData.run{  editedListData.idData=0 ; getData() }).also{
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
            Toast.makeText(applicationContext, R.string.activity_list_editor_save_OK, Toast.LENGTH_SHORT).show()
            finish()
        }

        //Delete button listener
        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_list_editor_dialog_remove_note_positive_button) { _, _ ->
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
                            db.dataDao().delete(editedListData.getData())
                        }
                    }
                    finish()
                }
                setNegativeButton(R.string.activity_list_editor_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_list_editor_dialog_remove_note)
                create()
            }.show()
        }

        //Share button listener
        binding.shareButton.setOnClickListener {
            var noteContent = ""
            editedListData.itemsList.forEach {
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

        //Add list item button listener
        binding.addListItemButton.setOnClickListener {
            editedListData.itemsList.add(ListData.ListItem())
            binding.listRecyclerView.adapter?.notifyItemInserted(editedListData.itemsList.size-1)
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

    /** Load [Data] and [Note] form database */
    private fun loadFromDB(){
        if(noteID != 0){
            editedNote = db.noteDao().getNoteById(noteID)
        }
        if(dataID != 0) {
            editedListData = ListData().apply {
                loadData(db.dataDao().getDataById(dataID))
            }
            if(noteID == 0){
                noteID = editedListData.noteID
                editedNote = db.noteDao().getNoteById(noteID)
            }
        }
        else{
            editedListData.noteID = noteID
        }
    }

    /** Recycler view initialization. Should be running on UI thread*/
    private fun initRecyclerView(){
        binding.listRecyclerView.apply {
            layoutManager = FlexboxLayoutManager(thisActivity)
            setItemViewCacheSize(256)
            adapter = ListEditorAdapter(editedListData).apply {
                attachItemTouchHelperToRecyclerView(binding.listRecyclerView)
            }
        }
    }

}
