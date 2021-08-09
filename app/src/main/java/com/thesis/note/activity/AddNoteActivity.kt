package com.thesis.note.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
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
import com.thesis.note.database.NoteType
import com.thesis.note.databinding.ActivityAddNoteBinding
//TODO documentation
class AddNoteActivity
    : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback{
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityAddNoteBinding
    private val activityContext = this

    var noteType : NoteType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityAddNoteLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        binding.addTextNote.setOnClickListener{
                startActivity(Intent(this,TextEditorActivity::class.java).apply{
                    putExtra("noteID", -1)
                    putExtra("dataID", -1)
                })
                finish()
            }
        binding.addImageNote.setOnClickListener {
            noteType = NoteType.Photo
            askForPermissionAndStartImageNote()
        }
        binding.addSoundNote.setOnClickListener {
            noteType = NoteType.Sound
            askForPermissionAndStartSoundNote()
        }
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

    private fun askForPermissionAndStartSoundNote() {
        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (permission != PERMISSION_GRANTED) {
            val permissionsArray = arrayOf(
                Manifest.permission.RECORD_AUDIO
            )
            ActivityCompat.requestPermissions(this, permissionsArray, 1)
        }
        else{
            startActivity(Intent(this,SoundEditorActivity::class.java).apply{
                putExtra("noteID", -1)
                putExtra("dataID", -1)
            })
            finish()
        }
    }

    private fun askForPermissionAndStartImageNote() {
        val permission = ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PERMISSION_GRANTED) {
            val permissionsArray = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(activityContext, permissionsArray, 1)
        }
        else{
            startActivity(Intent(this, ImageNoteActivity::class.java).apply{
                putExtra("noteID", -1)
                putExtra("dataID", -1)
            })
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.all { x -> x == PERMISSION_GRANTED }){
           when(noteType){
               NoteType.Sound -> {
                   startActivity(Intent(this,SoundEditorActivity::class.java).apply{
                       putExtra("noteID", -1)
                       putExtra("dataID", -1)
                   })
                   finish()
               }
               NoteType.Photo -> {
                   startActivity(Intent(this, ImageNoteActivity::class.java).apply{
                       putExtra("noteID", -1)
                       putExtra("dataID", -1)
                   })
                   finish()
               }
               else -> {}
           }
        }
        else{
            Toast.makeText(applicationContext, R.string.no_permissions, Toast.LENGTH_SHORT).show()
        }
    }
}
