package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.DrawerActivity
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
 * When creating [Intent] of this activity, you should put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * If passed id equals "-1" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "-1".
 *
 */
class ListEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityListEditorLayoutBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Data] id */
    private var dataID:Int = -1

    /** Edited [ListData] */
    private var listData = ListData()

    /** Edited [Note] id */
    private var noteID:Int = -1

    /** Edited [Note] */
    private lateinit var editedNote:Note

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListEditorLayoutBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase.invoke(this)
        listData.itemsList.add(ListData.ListItem())
        loadParameters()
        GlobalScope.launch {
            if(noteID != -1)
                loadData()
            runOnUiThread { initRecyclerView() }
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
            when {
                dataID != -1 -> {
                    //update
                    GlobalScope.launch {
                        db.dataDao().update(listData.getData())
                        db.noteDao().update(editedNote.apply { Date = Date() })
                    }
                }
                noteID != -1 -> {
                    //add new data to db
                    GlobalScope.launch {
                        val addedData = db.dataDao().insertAll(listData.run { this.idData = 0; this.noteID = thisActivity.noteID; getData() })
                        db.noteDao().update(editedNote.apply { Date = Date(); if(MainData==null) MainData=addedData[0].toInt()})
                    }
                }
                else -> {
                    GlobalScope.launch {
                        //add new note
                        db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White)).also {
                            noteID = it[0].toInt()
                            listData.noteID = noteID
                        }
                        db.dataDao().insertAll(listData.run{  listData.idData=0 ; getData() }).also{
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
                    if (dataID != -1){
                        GlobalScope.launch {
                            if (editedNote.MainData == dataID){
                                with(db.dataDao().getDataFromNote(noteID).map { it.IdData }.toMutableList()){
                                    remove(dataID)
                                    if(size == 0)
                                        editedNote.MainData = null
                                    else
                                        editedNote.MainData = this[0]
                                    db.noteDao().update(editedNote)
                                }
                            }
                            db.dataDao().delete(listData.getData())
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

        //Add list item button listener
        binding.addListItemButton.setOnClickListener {
            listData.itemsList.add(ListData.ListItem())
            binding.listRecyclerView.adapter?.notifyItemInserted(listData.itemsList.size-1)
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

    /** Recycler view initialization. Should be running on UI thread*/
    private fun initRecyclerView(){
        binding.listRecyclerView.apply {
            layoutManager = FlexboxLayoutManager(thisActivity)
            setItemViewCacheSize(256)
            adapter = ListEditorAdapter(listData).apply {
                attachItemTouchHelperToRecyclerView(binding.listRecyclerView)
            }
        }
    }

    /** Load [Data] with id [dataID] from database */
    private fun loadData(){
        editedNote = db.noteDao().getNoteById(noteID)
        runOnUiThread {
            //Set background color
            binding.root.background = ResourcesCompat.getDrawable(
                resources,
                NoteColorConverter.enumToColor(editedNote.Color),
                null
            )
        }
        if(dataID != -1) {
            listData = ListData().apply {
                loadData(db.dataDao().getDataById(dataID))
            }
        }
        else{
            listData.noteID = noteID
        }
    }
}
