package com.thesis.note.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.thesis.note.NavigationDrawer
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import kotlinx.android.synthetic.main.activity_image_note.*

import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//TODO
class ImageNoteActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

  //  var dataExistInDB = false
    var noteID = -1
    var dataID = -1

    var imageUri: Uri? = null
    private lateinit var db: AppDatabase
    private var imageView: ImageView? = null

    val activityContext = this


    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            //Handle loaded image from gallery
            imageUri = result.data?.data
            imageView!!.setImageURI(imageUri)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_note)
        drawerLayout = activity_image_note_layout
        navigationDrawer = NavigationDrawer(drawerLayout)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.abdt, R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase.invoke(this)
        imageView = findViewById<View>(R.id.imageView) as ImageView
        //TODO ask for permissions earlier
        //TODO error when saving without permissions
        askForPermisions()
        //set listener for open gallery button
        openGalleryButton.
        setOnClickListener { startForResult.launch(Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        ))}


        val parameters = intent.extras

        if(parameters != null){
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
            if(dataID == -1){
                //open gallery and load image
                //TODO error with asking for permissions
                //startForResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI))
            }
            else{
                //load image from storage
                    GlobalScope.launch {
                val data = db.dataDao().getDataById(noteID)
                imageUri = Uri.parse(data.Content)
                imageView!!.setImageURI(imageUri)
                    }
            }
        }

        //save button
        //TODO cleanup
        saveButton.setOnClickListener {
            if(dataID == -1) {
                //create data,copy to storage
                //     Environment.getExternalStorageDirectory() + File.separator + "myApp" + File.separator
                val sth = activityContext.applicationContext.getExternalFilesDir(null)
                var nUri = imageUri?.path
                nUri = nUri?.drop(6)
                val imageFile = File(nUri)

                debugImageNote.text = sth?.path
                //val toF = imageUri?.toFile()

                val currenttime = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
                val fileNameDate = "/image"+currenttime.format(Date())
                val fileNameToCopy = File(sth,fileNameDate)
                fileNameToCopy.createNewFile()
                debugImageNote.text = fileNameToCopy.path
                val newFile = imageFile?.copyTo(fileNameToCopy, true)

             //   val newFile = imageFile?.copyTo(sth!!, true)
             //   val currenttime = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")

              //  newFile.renameTo(File(sth,"image"+currenttime.toPattern()))

                //db.dataDao().insertAll(Data(0,))
                //create data


                if (noteID == -1) {
                    //create new note , create new data, open note
                   GlobalScope.launch {
                       val newNote =
                           db.noteDao().insertAll(Note(0, "", null, null, false, null, null, null))

                       val newData = db.dataDao().insertAll(
                           Data(
                               0,
                               newNote[0].toInt(),
                               NoteType.Photo,
                               newFile.path,
                               null
                           )
                       )

                       val note = db.noteDao().getNoteById(newNote[0].toInt())
                       note.MainData = newData[0].toInt()
                       db.noteDao().updateTodo(note)
                   }
                    //TODO open note
                }
                else{
                    GlobalScope.launch {
                    val newData = db.dataDao().insertAll(Data(0,noteID,NoteType.Photo,newFile.path,null))}
                }
            }
            else{
                //update image
                val sth = activityContext.applicationContext.getExternalFilesDir(null)
                var nUri = imageUri?.path
                nUri = nUri?.drop(6)
                val imageFile = File(nUri)

                debugImageNote.text = sth?.path
                //val toF = imageUri?.toFile()


                val currenttime = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
                val fileNameDate = "/image"+currenttime.format(Date())
                val fileNameToCopy = File(sth,fileNameDate)
                fileNameToCopy.createNewFile()
                debugImageNote.text = fileNameToCopy.path
                val newFile = imageFile?.copyTo(fileNameToCopy, true)

                GlobalScope.launch {
                    val dataUpdate = db.dataDao().getDataById(dataID)
                    dataUpdate.Content = newFile.path
                    db.dataDao().updateTodo(dataUpdate)
                }


            }
            Toast.makeText(applicationContext, "ZAPISANO", Toast.LENGTH_SHORT).show()
            finish()
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
                ActivityCompat.requestPermissions(
                    activityContext,
                    PERMISSIONS_STORAGE,
                    1
                )
            }
    }
}
