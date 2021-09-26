package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.thesis.note.R
import com.thesis.note.SoundPlayer
import com.thesis.note.SoundRecorder
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.Color
import com.thesis.note.database.ColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityRecordingEditorBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 *  Activity for editing recording notes.
 *
 * When creating [Intent] of this activity, you can put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * Activity will load [Note] and [Data] with passed id.
 * If passed id equals "0" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "0".
 *
 */
class RecordingEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityRecordingEditorBinding

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

    /** Current [SoundPlayer]*/
    private lateinit var soundPlayer : SoundPlayer

    /** Current [SoundRecorder] */
    private lateinit var soundRecorder: SoundRecorder

    /**  Loading data, layout init and setting listeners. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordingEditorBinding.inflate(layoutInflater)
        loadSettings()
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase(this)
        loadParameters()
        GlobalScope.launch {
            loadData()
            runOnUiThread {
                setSoundPlayerIsEnabled(false)
                initSoundPlayer()
                initSoundRecorder()
            }
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
            when{
                soundRecorder.isWorking -> {
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_recording, Toast.LENGTH_SHORT).show()
                }
                !soundPlayer.isFileOpen() -> {
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
                }
                soundPlayer.filePath == "" -> {
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
                }
                dataID != 0 -> {
                    //update
                    GlobalScope.launch {
                        db.dataDao().update(db.dataDao().getDataById(dataID).apply {
                            Content = soundPlayer.filePath ?:""
                            db.noteDao().update(db.noteDao().getNoteById(NoteId).apply { Date = Date() })
                        })
                    }
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_save_OK, Toast.LENGTH_SHORT).show()
                    finish()
                }
                noteID != 0 -> {
                    //add new data to db
                    GlobalScope.launch {
                        db.dataDao().insert(Data(0, noteID, NoteType.Recording, soundPlayer.filePath ?:"", null,null,null))
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
                            .insert(Note(0, "", null, null, false, null, Date(), null, Color.White))
                            .let{ db.noteDao().getNoteById(it.toInt()) }
                        val newDataID = db
                            .dataDao()
                            .insert(Data(0, newNote.IdNote, NoteType.Recording, soundPlayer.filePath ?:"", null, null,null))
                        db.noteDao().update(newNote.apply { MainData = newDataID.toInt() })
                        startActivity(noteViewerActivityIntent.apply { putExtra("noteID", newNote.IdNote) })
                    }
                    Toast.makeText(thisActivity, R.string.activity_recording_editor_save_OK, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        //Delete button listener
        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_recording_editor_dialog_remove_note_positive_button) { _, _ ->
                    if (dataID != 0){
                        GlobalScope.launch {
                            if (editedNote?.MainData == editedData?.IdData){
                                with(db.dataDao().getDataFromNote(noteID).map { it.IdData }.toMutableList()){
                                    remove(editedData?.IdData)
                                    if(size == 0)
                                        editedNote?.MainData = null
                                    else
                                        editedNote?.MainData = this[0]
                                    editedNote?.let { it1 -> db.noteDao().update(it1) }
                                }
                            }
                            editedData?.let { it1 ->
                                db.dataDao().delete(it1)
                                try{ File(it1.Content).delete() }catch(ex:Exception){}
                            }
                        }
                    }
                    finish()
                }
                setNegativeButton(R.string.activity_recording_editor_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_recording_editor_dialog_remove_note)
                create()
            }.show()
        }

        //Share button listener
        binding.shareButton.setOnClickListener {
            when{
                soundRecorder.isWorking -> {
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_recording, Toast.LENGTH_SHORT).show()
                }
                !soundPlayer.isFileOpen() -> {
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
                }
                soundPlayer.filePath == "" -> {
                    Toast.makeText(applicationContext, R.string.activity_recording_editor_no_recording, Toast.LENGTH_SHORT).show()
                }
                else ->{
                    Intent(Intent.ACTION_SEND).apply{
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(
                                thisActivity,
                                "com.thesis.note.fileprovider",
                                File(soundPlayer.filePath?:"")
                            )
                        )
                        startActivity(Intent.createChooser(this, getString(R.string.activity_image_note_share)))
                        //startActivity(this)
                    }
                }
            }
        }
    }

    /** On pause callback. Releasing [soundPlayer] and [soundRecorder] */
    override fun onPause() {
        super.onPause()
        soundPlayer.release()
        soundRecorder.release()

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
                editedNote?.Color?.let {
                    binding.root.background =
                        ResourcesCompat.getDrawable(resources, ColorConverter.enumToColor(it), null)
                }
            }
        }
    }

    /** Create new [SoundPlayer] and bind it with gui */
    private fun initSoundPlayer(){
        soundPlayer = SoundPlayer(thisActivity).apply {
            currentPositionTextView = binding.soundPlayerTimeNow
            durationTextView = binding.soundPlayerAllTime
            handler = Handler(Looper.getMainLooper())
            editedData?.Content?.let { setSoundPlayerIsEnabled(openFile(it)) }
            binding.soundPlayerPlayButton.setOnClickListener { play() }
            binding.soundPlayerPauseButton.setOnClickListener { pause() }
            binding.soundPlayerStopButton.setOnClickListener { stop() }
            onStartPlayingListener = { setSoundRecorderIsEnabled(false) }
            onEndPlayingListener = { setSoundRecorderIsEnabled(true) }
            onPausePlayingListener = { setSoundRecorderIsEnabled(true) }
        }
    }

    /** Create new [SoundRecorder] and bind it with gui */
    private fun initSoundRecorder(){
        soundRecorder = SoundRecorder(thisActivity).apply {
            filePath = createFile().absolutePath
            durationTextView = binding.soundRecorderTimeNow
            handler = Handler(Looper.getMainLooper())
            binding.soundRecorderRecordButton.setOnClickListener { if(isWorking) stopRecording() else startRecording() }
            binding.soundRecorderCancelButton.setOnClickListener { cancelRecording() }
            onStartRecordingListener = {
                setSoundPlayerIsEnabled(false)
                binding.soundRecorderRecordButton.setBackgroundResource(R.drawable.ic_baseline_check)
            }
            onStopRecordingListener = { filePath ->
                filePath?.let{setSoundPlayerIsEnabled(soundPlayer.openFile(it))}
                binding.soundRecorderRecordButton.setBackgroundResource(R.drawable.ic_baseline_fiber_manual_record)
            }
            onCancelRecordingListener = {
                if(editedData==null){
                    soundPlayer.closeFile()
                    setSoundPlayerIsEnabled(false)
                }
                else{
                    editedData?.Content?.let { setSoundPlayerIsEnabled(soundPlayer.openFile(it))}
                }
                binding.soundRecorderRecordButton.setBackgroundResource(R.drawable.ic_baseline_fiber_manual_record)
            }
        }
    }

    /** Create new file */
    private fun createFile():File {
        val timeStamp: String = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
        val storageDir: File? = thisActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("audio_${timeStamp}_", ".amr", storageDir).apply { createNewFile() }
    }

    /** Set if Sound Player GUI is enabled */
    private fun setSoundPlayerIsEnabled(value:Boolean){
        binding.soundPlayerPlayButton.isEnabled = value
        binding.soundPlayerPauseButton.isEnabled = value
        binding.soundPlayerStopButton.isEnabled = value
    }

    /** Set if Sound Recorder GUI is enabled */
    private fun setSoundRecorderIsEnabled(value:Boolean){
        binding.soundRecorderRecordButton.isEnabled = value
        binding.soundRecorderCancelButton.isEnabled = value
    }

    /** Load settings related to this activity */
    private fun loadSettings(){
        binding.deleteButton.also { item ->
            with(sharedPreferences.getBoolean("recording_editor_delete", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
        binding.shareButton.also { item ->
            with(sharedPreferences.getBoolean("recording_editor_share", true)) {
                item.isEnabled = this
                item.visibility = if(this) View.VISIBLE else View.GONE
            }
        }
    }

}
