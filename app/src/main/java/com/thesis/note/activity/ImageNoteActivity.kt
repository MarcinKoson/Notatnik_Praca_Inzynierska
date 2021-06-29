package com.thesis.note.activity

import android.R.attr.data
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.thesis.note.NavigationDrawer
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import kotlinx.android.synthetic.main.activity_image_note.*
import kotlinx.android.synthetic.main.template_empty_layout.navigationView
import kotlinx.android.synthetic.main.template_empty_layout.toolbar
import java.io.File

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
        drawerLayout = image_note_drawer_layout
        navigationDrawer = NavigationDrawer(drawerLayout)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase.invoke(this)
        imageView = findViewById<View>(R.id.imageView) as ImageView

        //findViewById<View>(R.id.openGalleryButton).
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
                startForResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI))
            }
            else{
                //load image from storage
                val data = db.dataDao().getDataById(noteID)
                imageUri = Uri.parse(data.Content)
                imageView!!.setImageURI(imageUri)
            }
        }

        //save button
        saveButton.setOnClickListener {
            if(dataID == -1){
                //create data,copy to storage
           //     Environment.getExternalStorageDirectory() + File.separator + "myApp" + File.separator
                val sth = activityContext.applicationContext.getExternalFilesDir(null)

                //File(sth, "testF").mkdir()

                var nUri = imageUri?.path
                nUri = nUri?.drop(6)
                val imageFile = java.io.File(nUri)

                debugImageNote.text =sth?.path
               //val toF = imageUri?.toFile()

               imageFile?.copyTo(sth!!,true)
                Toast.makeText(applicationContext,"ZAPISANO", Toast.LENGTH_SHORT).show()



                if(noteID == -1){
                    //create note, open note
                }
                else{
                    //save data
                }
            }
            else{
                //update image
            }
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
}
