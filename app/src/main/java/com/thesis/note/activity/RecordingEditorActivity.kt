package com.thesis.note.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityRecordingEditorBinding
import com.thesis.note.fragment.SoundPlayer
import com.thesis.note.fragment.SoundRecorder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
/**
 *  Activity for editing recording notes.
 *
 * When creating [Intent] of this activity, you should put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * If passed id equals "-1" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "-1".
 *
 */
//TODO check - sometime crash
class RecordingEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityRecordingEditorBinding

    /** View model for [SoundPlayer] */
    private val soundPlayerViewModel: SoundPlayer.SoundPlayerViewModel by viewModels()

    /** View model for [SoundRecorder] */
    private val soundRecorderViewModel: SoundRecorder.SoundRecorderViewModel by viewModels()

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Data] id */
    private var dataID:Int = -1

    /** Edited [Data] */
    private var editedData : Data? = null

    /** Edited [Note] id */
    private var noteID:Int = -1

    /** Edited [Note] */
    private lateinit var editedNote: Note

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingEditorBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase(this)
        loadParameters()
        GlobalScope.launch {
            runOnUiThread {
                //supportFragmentManager.commit {
                //    setReorderingAllowed(true)
                //    add<SoundPlayer>(R.id.fragment_player)
               // }

            }
            loadData()
            if(editedData == null) {
                //runOnUiThread { soundPlayerViewModel.setIsEnabled(false) }
            }
            else{
                runOnUiThread {
                    soundPlayerViewModel.setIsEnabled(true)
                    soundPlayerViewModel.setFilePath(editedData?.Content?:"")

                    //fragment.disable()

                }
            }
        }
        ///soundPlayerViewModel.isWorking.observe(this, { soundRecorderViewModel.setIsEnabled(!it)})
        val fragment: SoundPlayer = supportFragmentManager.findFragmentById(R.id.fragment_player) as SoundPlayer
        fragment.onStartPlayingListener = { soundRecorderViewModel.setIsEnabled(false) }
        fragment.onEndPlayingListener = { soundRecorderViewModel.setIsEnabled(true) }
        fragment.onPausePlayingListener = { soundRecorderViewModel.setIsEnabled(true) }
       // fragment.setIsEnabled(true)

        with(soundRecorderViewModel){
            setOutputFile(createFile())
            isWorking.observe(thisActivity, { fragment.setIsEnabled(!it) })
            //isWorking.observe(thisActivity, { fragment.view?.isEnabled = !it })
            setOnRecordingEndListener { filePath ->
                soundPlayerViewModel.setFilePath(filePath)
            }
            setOnRecordingCancelListener {
                if(editedData != null)
                    soundPlayerViewModel.setFilePath(editedData!!.Content)
                else
                    soundPlayerViewModel.setIsEnabled(false)
            }
            setIsEnabled(true)
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
           when{
               soundRecorderViewModel.isWorking.value == true -> {
                   Toast.makeText(applicationContext, R.string.activity_recording_editor_recording, Toast.LENGTH_SHORT).show()
               }
               soundPlayerViewModel.isEnabled.value == false -> {
                   Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
               }
               soundPlayerViewModel.filePath.value == "" -> {
                   Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
               }
               dataID != -1 -> {
                   //update
                   GlobalScope.launch {
                       db.dataDao().update(db.dataDao().getDataById(dataID).apply {
                           Content = soundPlayerViewModel.filePath.value?:""
                           db.noteDao().update(db.noteDao().getNoteById(NoteId).apply { Date = Date() })
                       })
                   }
                   Toast.makeText(applicationContext, R.string.activity_recording_editor_save_OK, Toast.LENGTH_SHORT).show()
                   finish()
               }
               noteID != -1 -> {
                   //add new data to db
                   GlobalScope.launch {
                       db.dataDao().insertAll(Data(0, noteID, NoteType.Recording, soundPlayerViewModel.filePath.value?:"", null,null,null))
                       db.noteDao().update(db.noteDao().getNoteById(noteID).apply { Date = Date() })
                   }
                   Toast.makeText(applicationContext, R.string.activity_recording_editor_save_OK, Toast.LENGTH_SHORT).show()
                   finish()
               }
               else -> {
                   //create intent for note viewer
                   val noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
                   //create new Note and Data
                   GlobalScope.launch {
                       val newNote = db
                           .noteDao()
                           .insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White))
                           .let{ db.noteDao().getNoteById(it[0].toInt()) }
                       val newDataID = db
                           .dataDao()
                           .insertAll(Data(0, newNote.IdNote, NoteType.Recording, soundPlayerViewModel.filePath.value?:"", null, null,null))
                       db.noteDao().update(newNote.apply { MainData = newDataID[0].toInt() })
                       startActivity(noteViewerActivityIntent.apply { putExtra("noteID", newNote.IdNote) })
                   }
                   Toast.makeText(thisActivity, R.string.activity_recording_editor_save_OK, Toast.LENGTH_SHORT).show()
                   finish()
               }
           }
        }

        //TODO Delete button listener - test
        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_image_note_dialog_remove_note_positive_button) { _, _ ->
                    if (dataID != -1){
                        GlobalScope.launch {
                            if (editedNote.MainData == dataID){
                                with(db.dataDao().getDataFromNote(noteID).map { it.IdData }.toMutableList()){
                                    remove(dataID)
                                    if(size == 0)
                                        editedNote.MainData = null
                                    else
                                        editedNote.MainData = this[0]
                                }
                                db.noteDao().update(editedNote)
                            }
                            editedData?.let { it1 -> db.dataDao().delete(it1) }
                        }
                    }
                    finish()
                }
                setNegativeButton(R.string.activity_image_note_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_image_note_dialog_remove_note)
                create()
            }.show()
        }

        //TODO Share button listener - use file provider
        binding.shareButton.setOnClickListener {
            if(soundPlayerViewModel.isEnabled.value != false && soundPlayerViewModel.filePath.value != "" )
                Intent(Intent.ACTION_SEND).apply{
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, Uri.parse(soundPlayerViewModel.filePath.value))
                    startActivity(Intent.createChooser(this, getString(R.string.activity_recording_editor_share)))
                    //startActivity(this)
                }
            else{
                Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
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

    /** Load data with id [dataID]. It is accessing database */
    private fun loadData() {
        if(dataID != -1)
        {
            editedData = db.dataDao().getDataById(dataID)
            editedNote = db.noteDao().getNoteById(noteID)
            runOnUiThread{
                //Set background color
                binding.root.background = ResourcesCompat.getDrawable(
                    resources,
                    NoteColorConverter.enumToColor(editedNote.Color),
                    null
                )
            }
        }
    }

    /** Create new file */
    private fun createFile():File {
        val timeStamp: String = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
        val storageDir: File? = thisActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("audio_${timeStamp}_", ".amr", storageDir).apply { createNewFile() }
    }

}
