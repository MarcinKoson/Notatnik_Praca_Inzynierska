package com.thesis.note.activity

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Data
import com.thesis.note.databinding.ActivitySoundEditorBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SoundEditorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivitySoundEditorBinding
    private lateinit var db: AppDatabase

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false

    private var noteID = -1
    private var dataID = -1

    private var data : Data? = null
    private var filePath= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoundEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activitySoundEditorLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.abdt, R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        //TODO move to add note
        val permission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1)

        }

        //------------
        db = AppDatabase.invoke(this)
        val parameters = intent.extras
        if (parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
        }
        if (noteID == -1) {
            //create new file
            val timeStamp: String =
                SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val newFile = File.createTempFile("audio_${timeStamp}_", ".amr", storageDir)
            newFile.createNewFile()
            filePath = newFile.path
        } else {
            GlobalScope.launch {
                data = db.dataDao().getDataById(dataID)
                filePath = data?.Content!!
                binding.soundEditorStatus.text = getString(R.string.sound_editor_recorded)
            }
        }
        binding.recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
        binding.playButton.setOnClickListener {
            startPlaying()
        }
        binding.saveButton.setOnClickListener {
            //TODO saving sound notes
        }
    }

    override fun onPause() {
        super.onPause()

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaPlayer?.apply {
            stop()
            release()
        }
    }

    private fun startPlaying() {
        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                val fis =  FileInputStream(filePath)
                setDataSource(fis.fd)
                prepare()
                start()
            } catch (e: IOException) {
                Toast.makeText(applicationContext,  "ERROR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        binding.soundEditorStatus.text = getString(R.string.sound_editor_recording)
        isRecording = true
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        binding.soundEditorStatus.text = getString(R.string.sound_editor_recorded)
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
