package com.thesis.note.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.thesis.note.NavigationDrawer
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityImageNoteBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ImageNoteActivity
    :AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityImageNoteBinding
    private lateinit var db: AppDatabase

    //note&data ID
    private var noteID = -1
    private var dataID = -1

    //image
    private lateinit var currentPhotoPath: String
    private var imageState = ImageState.NoImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityImageNoteLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)
        val drawerToggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.abdt, R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        //open database
        db = AppDatabase.invoke(this)
        //get parameters
        val parameters = intent.extras
        if(parameters != null){
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
            if(noteID != -1){
                imageState = ImageState.OldImage
                //load image
                GlobalScope.launch {
                    val data = db.dataDao().getDataById(noteID)
                    //binding.imageView.setImageURI(Uri.parse(data.Content))
                    runOnUiThread{
                        setImage(binding,data.Content)
                    }
                }
            }
        }
        //set listener for open gallery button
        binding.openGalleryButton.setOnClickListener {
            galleryStartForResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI))
        }
        //set listener for open camera button
        binding.openCameraButton.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    }catch (ex: IOException) {
                        Toast.makeText(applicationContext, R.string.error_create_file, Toast.LENGTH_SHORT).show()
                        null
                    }
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(this, "com.thesis.note.fileprovider", it)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        cameraStartForResult.launch(takePictureIntent)
                    }
                }
            }
        }
        //save button
        binding.saveButton.setOnClickListener {
            if(imageState == ImageState.NoImage){
                Toast.makeText(applicationContext, R.string.activity_image_note_no_image, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(imageState == ImageState.NewGalleryImage || imageState == ImageState.NewCameraImage) {
                if(imageState == ImageState.NewGalleryImage){
                    saveImageFromGallery()
                }
                //save to DB
                if (noteID == -1 && dataID == -1) {
                    //create intent for note viewer
                    val noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
                    //create new Note and Data
                    GlobalScope.launch {
                        val newNoteID =
                            db.noteDao().insertAll(Note(0, "", null, null, false, null, null, null))
                        val newDataID = db.dataDao().insertAll(
                            Data(
                                0,
                                newNoteID[0].toInt(),
                                NoteType.Photo,
                                currentPhotoPath,
                                null
                            )
                        )
                        val newNote = db.noteDao().getNoteById(newNoteID[0].toInt())
                        newNote.MainData = newDataID[0].toInt()
                        db.noteDao().update(newNote)
                        //open note
                        noteViewerActivityIntent.putExtra("noteID", newNote.IdNote)
                        startActivity(noteViewerActivityIntent)
                    }
                }else if (dataID == -1) {
                    //create new Data
                    GlobalScope.launch {
                        db.dataDao().insertAll(Data(0, noteID, NoteType.Photo, currentPhotoPath, null))
                    }
                }else {
                    //update Data
                    GlobalScope.launch {
                        val dataUpdate = db.dataDao().getDataById(dataID)
                        dataUpdate.Content = currentPhotoPath
                        db.dataDao().update(dataUpdate)
                    }
                }
            }

            Toast.makeText(applicationContext, R.string.save_OK, Toast.LENGTH_SHORT).show()
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

    private fun saveImageFromGallery(){
        val oldFilePath = currentPhotoPath.drop(6) //remove "/raw/" from path
        val oldFile = File(oldFilePath)
        oldFile.copyTo(createImageFile(),true)
    }

    private val galleryStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageState = ImageState.NewGalleryImage
            //Handle loaded image from gallery
            val imageUri = result.data?.data
            currentPhotoPath = imageUri?.path!!
            //TODO image scaling
            // setImage(binding,currentPhotoPath)
            binding.imageView.setImageURI(imageUri)
        }

    }

    private val cameraStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageState = ImageState.NewCameraImage
            //load image
            setImage(binding,currentPhotoPath)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        //Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("image_${timeStamp}_",".jpg",storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun setImage(binding : ActivityImageNoteBinding, path:String?) {
        //TODO image scaling
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            inJustDecodeBounds = false
            inSampleSize = max(1, min(outWidth / 150, outHeight / 150))
        }
        binding.imageView.setImageBitmap(BitmapFactory.decodeFile(path, opts))
    }

    enum class ImageState(val id:Int) {
        NoImage(0),
        OldImage(1),
        NewCameraImage(2),
        NewGalleryImage(3)
    }

}
