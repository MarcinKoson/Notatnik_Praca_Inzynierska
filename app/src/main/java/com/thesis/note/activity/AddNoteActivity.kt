package com.thesis.note.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.thesis.note.databinding.ActivityAddNoteBinding


//TODO
class AddNoteActivity
    : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ActivityCompat.OnRequestPermissionsResultCallback{
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityAddNoteBinding
    val activityContext = this

    lateinit var ImageNoteIntent: Intent
    lateinit var SoundNoteIntent: Intent

    var noteType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawer_layout = binding.activityAddNoteLayout
        navigationDrawer = NavigationDrawer(drawer_layout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------

        val TextNoteIntent = Intent(this,TextEditorActivity::class.java)
        binding.button.setOnClickListener{
                startActivity(TextNoteIntent)
                finish()
            }

        ImageNoteIntent = Intent(this, ImageNoteActivity::class.java)
        ImageNoteIntent.putExtra("dataID", -1)
        ImageNoteIntent.putExtra("noteID", -1)
        binding.button4.setOnClickListener {
           noteType = 1
            askForPermisions()
        }

        SoundNoteIntent = Intent(this,SoundEditorActivity::class.java)

        binding.button3.setOnClickListener {
            noteType = 2
            askForSoundPremissions()
        }
    }

    private fun askForSoundPremissions() {
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
        else{
            startActivity(SoundNoteIntent)
            finish()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    private fun askForPermisions() {

        val permission = ActivityCompat.checkSelfPermission(
            activityContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activityContext, PERMISSIONS_STORAGE, 1)
        }
        else{
            startActivity(ImageNoteIntent)
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.all { x -> x == PERMISSION_GRANTED }){
           if(noteType == 1) {
               startActivity(ImageNoteIntent)
               finish()
           }
            else{
                startActivity(SoundNoteIntent)
                finish()
            }
        }
        else{
            Toast.makeText(applicationContext, "Brak uprawnień", Toast.LENGTH_SHORT).show()
        }

    }

}
