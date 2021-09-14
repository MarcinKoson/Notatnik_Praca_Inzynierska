package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
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
    private var data : Data? = null

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
            loadData()
            if(data == null) {
                runOnUiThread { soundPlayerViewModel.setIsEnabled(false) }
            }
            else{
                runOnUiThread {
                    soundPlayerViewModel.setIsEnabled(true)
                    soundPlayerViewModel.setFilePath(data?.Content?:"")
                }
            }
        }
        soundPlayerViewModel.isWorking.observe(this, { soundRecorderViewModel.setIsEnabled(!it)})

        with(soundRecorderViewModel){
            setOutputFile(createFile())
            isWorking.observe(thisActivity, { soundPlayerViewModel.setIsEnabled(!it)})
            setOnRecordingEndListener { filePath ->
                soundPlayerViewModel.setFilePath(filePath)
            }
            setOnRecordingCancelListener {
                if(data != null)
                    soundPlayerViewModel.setFilePath(data!!.Content)
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

        //TODO Delete button listener
        binding.deleteButton.setOnClickListener {
            Toast.makeText(thisActivity, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //TODO Share button listener
        binding.shareButton.setOnClickListener {
            Toast.makeText(thisActivity, R.string.not_implemented, Toast.LENGTH_SHORT).show()
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
            data = db.dataDao().getDataById(dataID)
            editedNote = db.noteDao().getNoteById(dataID)
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
