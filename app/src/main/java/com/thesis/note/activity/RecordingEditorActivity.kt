package com.thesis.note.activity

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.ListData
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityListEditorLayoutBinding
import com.thesis.note.databinding.ActivityRecordingEditorBinding
import com.thesis.note.databinding.ActivitySoundEditorBinding
import com.thesis.note.fragment.SoundPlayer
import com.thesis.note.fragment.SoundRecorder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Data] id */
    private var dataID:Int = -1

    /** Edited [Data] */
    private var data : Data? = null

    /** Edited [Note] id */
    private var noteID:Int = -1

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingEditorBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase(this)
        loadParameters()
        if(dataID != 1)
        {
            GlobalScope.launch {
                data = db.dataDao().getDataById(dataID)
                runOnUiThread { viewModel.setItem(data?.Content?:"") }
            }

        }
        binding.fragmentPlayer.isEnabled = true
        binding.fragmentRecorder.isEnabled = true
       // binding.fragmentRecorder.

        viewModelRecorder.listener.value = { v -> viewModel.setItem(v) }
                /*

        binding.saveButton.setOnClickListener {
           if(!isRecorded){
               Toast.makeText(applicationContext, R.string.sound_editor_no_recording, Toast.LENGTH_SHORT).show()
           }
            else if(isRecording) {
               Toast.makeText(applicationContext, R.string.sound_editor_recording, Toast.LENGTH_SHORT).show()
           }else{
               if (noteID == -1 && dataID == -1) {
                   //create intent for note viewer
                   val noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
                   //create new Note and Data
                   GlobalScope.launch {
                       val newNoteID =
                           db.noteDao().insertAll(Note(0, "", null, null, false, null, null, null,NoteColor.White))//TODO color size
                       val newDataID = db.dataDao().insertAll(
                           Data(
                               0,
                               newNoteID[0].toInt(),
                               NoteType.Recording,
                               filePath,
                               null, null,null))
                       val newNote = db.noteDao().getNoteById(newNoteID[0].toInt())
                       newNote.MainData = newDataID[0].toInt()
                       db.noteDao().update(newNote)
                       noteViewerActivityIntent.putExtra("noteID", newNote.IdNote)
                       startActivity(noteViewerActivityIntent)
                   }
               }else if (dataID == -1) {
                   //create new Data
                   GlobalScope.launch {
                       db.dataDao().insertAll(Data(0, noteID, NoteType.Recording, filePath, null,null,null))
                   }
               }else {
                   //update Data
                   GlobalScope.launch {
                       val dataUpdate = db.dataDao().getDataById(dataID)
                       dataUpdate.Content = filePath
                       db.dataDao().update(dataUpdate)
                   }
               }
               Toast.makeText(applicationContext, R.string.save_OK, Toast.LENGTH_SHORT).show()
               finish()
            }
        }

         */
    }

    /** Load parameters passed from another activity */
    private fun loadParameters() {
        val parameters = intent.extras
        if (parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
        }
    }
    private val viewModel: SoundPlayer.SoundPlayerViewModel by viewModels()
    private val viewModelRecorder: SoundRecorder.SoundRecorderViewModel by viewModels()






    private var isRecording = false
    private var isRecorded = false
    private var filePath= ""



    override fun onPause() {
        super.onPause()
/*
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaPlayer?.apply {
            stop()
            release()
        }
        */

    }

    /*


*/

}
