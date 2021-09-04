package com.thesis.note.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityImageNoteBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 *  Activity for image editing.
 *
 *  When creating [Intent] of this activity, you should put extended data with
 *  putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 *  If passed id equals "-1" activity interprets this as new data or new note.
 *  Default value for [noteID] and [dataID] is "-1".
 */
class ImageNoteActivity : DrawerActivity()
{
    /** This activity */
    private val thisActivity = this

    /** View binding */
    private lateinit var binding: ActivityImageNoteBinding

    /** Database */
    private lateinit var db: AppDatabase

    /** Edited [Note] id */
    private var noteID = -1

    /** Edited [Data] id */
    private var dataID = -1

    /** Edited [Data] */
    private lateinit var editedData: Data

    /** Current path of image */
    private lateinit var currentImagePath: String

    /** State of current loaded image */
    private var imageState = ImageState.NoImage

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageNoteBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        //open database
        db = AppDatabase(this)
        loadParameters()
        if(noteID != -1){
            loadData()
        }

        //Save button listener
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
                            db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null,NoteColor.White))
                        val newDataID = db.dataDao().insertAll(
                            Data(
                                0,
                                newNoteID[0].toInt(),
                                NoteType.Image,
                                currentImagePath,
                                null,
                                null,null
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
                        db.dataDao().insertAll(Data(0, noteID, NoteType.Image, currentImagePath,null,null,null))
                        db.noteDao().update(db.noteDao().getNoteById(noteID).also { it.Date = Date()})
                    }
                }else {
                    //update Data
                    GlobalScope.launch {
                        val dataUpdate = db.dataDao().getDataById(dataID)
                        dataUpdate.Content = currentImagePath
                        dataUpdate.Info = null
                        db.dataDao().update(dataUpdate)
                        db.noteDao().update(db.noteDao().getNoteById(noteID).also { it.Date = Date()})
                    }
                }
            }
            Toast.makeText(applicationContext, R.string.activity_image_save_OK, Toast.LENGTH_SHORT).show()
            finish()
        }

        //TODO Delete button listener
        binding.deleteButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //TODO Share button listener
        binding.shareButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //Open gallery button listener
        binding.openGalleryButton.setOnClickListener {
            galleryStartForResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI))
        }

        //Open camera button listener
        binding.openCameraButton.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    }catch (ex: IOException) {
                        Toast.makeText(applicationContext, R.string.activity_image_note_error_create_file, Toast.LENGTH_SHORT).show()
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
    }

    /** Load parameters passed from another activity */
    private fun loadParameters(){
        val parameters = intent.extras
        if(parameters != null){
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")

        }
    }

    /** Load [Data] with id [dataID]  */
    private fun loadData(){
        GlobalScope.launch {
            editedData = db.dataDao().getDataById(noteID)
            runOnUiThread{
                setImage(editedData.Content)
                imageState = ImageState.OldImage
            }
        }
    }

    /** Register a request to start an activity for result for getting image from gallery */
    private val galleryStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageState = ImageState.NewGalleryImage
            //Handle loaded image from gallery
            val imageUri = result.data?.data
            currentImagePath = imageUri?.path!!
            //TODO remove drop
            currentImagePath = currentImagePath.drop(6)  //remove "/raw/" from path
            setImage(currentImagePath)
        }
    }

    /** Register a request to start an activity for result for getting image from camera */
    private val cameraStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageState = ImageState.NewCameraImage
            //load image
            setImage(currentImagePath)
        }
    }

    /** Save image from gallery to external storage of this application */
    private fun saveImageFromGallery(){
        val originalFile = File(currentImagePath)
        originalFile.copyTo(createImageFile(),true)
    }

    /** Create [File] for image with name containing current time stamp */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        //Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "image_${timeStamp}.jpg").apply {
            currentImagePath = absolutePath
            createNewFile()
        }
    }

    /** Set image from given [path]. */
    private fun setImage(path:String?){
        Glide.with(thisActivity)
            .load(path)
            .fitCenter()
            .placeholder(R.drawable.ic_loading)
            .into(binding.chosenImage)
    }

    /** Enum class with possible states of showed image */
    enum class ImageState(val id:Int) {
        NoImage(0),
        OldImage(1),
        NewCameraImage(2),
        NewGalleryImage(3)
    }

}
