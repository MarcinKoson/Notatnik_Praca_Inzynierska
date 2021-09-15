package com.thesis.note.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityImageNoteBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

    /** Edited [Note] */
    private lateinit var editedNote: Note

    /** Edited [Data] id */
    private var dataID = -1

    /** Edited [Data] */
    private lateinit var editedData: Data

    /** Image from camera*/
    private var cameraImage : File? = null

    /** Image from gallery*/
    private var galleryImage : Bitmap? = null

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
            when(imageState){
                ImageState.NoImage -> {
                    Toast.makeText(applicationContext, R.string.activity_image_note_no_image, Toast.LENGTH_SHORT).show()
                }
                ImageState.OldImage -> {
                    Toast.makeText(applicationContext, R.string.activity_image_save_OK, Toast.LENGTH_SHORT).show()
                    finish()
                }
                ImageState.NewGalleryImage -> {
                    try {
                        saveImageToDB(createImageFile().also {
                            FileOutputStream(it).run {
                                galleryImage?.compress(Bitmap.CompressFormat.JPEG, 85, this)
                                flush()
                                close()
                            }
                        })
                        Toast.makeText(applicationContext, R.string.activity_image_save_OK, Toast.LENGTH_SHORT).show()
                        finish()
                    }catch (ex : IOException){
                        Toast.makeText(applicationContext, R.string.activity_image_save_ERROR, Toast.LENGTH_SHORT).show()
                    }
                }
                ImageState.NewCameraImage -> {
                    cameraImage?.let { image -> saveImageToDB(image) }
                    Toast.makeText(applicationContext, R.string.activity_image_save_OK, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        //TODO Delete button listener
        binding.deleteButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //Share button listener
        binding.shareButton.setOnClickListener {
            when(imageState){
                ImageState.NoImage ->{
                    Toast.makeText(applicationContext, R.string.activity_image_note_no_image, Toast.LENGTH_SHORT).show()
                }
                ImageState.NewCameraImage->{
                    Toast.makeText(applicationContext, R.string.activity_image_note_share_error, Toast.LENGTH_SHORT).show()
                }
                ImageState.NewGalleryImage ->{
                    Toast.makeText(applicationContext, R.string.activity_image_note_share_error, Toast.LENGTH_SHORT).show()
                }
                ImageState.OldImage -> {
                    Intent(Intent.ACTION_SEND).apply{
                        type = "image/jpg"
                        putExtra(Intent.EXTRA_STREAM,getUriForFile(thisActivity,"com.thesis.note.fileprovider", File(editedData.Content)))
                        startActivity(Intent.createChooser(this, getString(R.string.activity_image_note_share)))
                        //startActivity(this)
                    }
                }
            }
        }

        //Open gallery button listener
        binding.openGalleryButton.setOnClickListener {
            galleryStartForResult.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        //Open camera button listener
        binding.openCameraButton.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    }catch (ex: IOException) {
                        Toast.makeText(thisActivity, R.string.activity_image_note_error_create_file, Toast.LENGTH_SHORT).show()
                        null
                    }
                    photoFile?.also {
                        cameraImage = photoFile
                        val photoURI: Uri = getUriForFile(this, "com.thesis.note.fileprovider", it)
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
            editedNote = db.noteDao().getNoteById(dataID)
            runOnUiThread{
                setImage(editedData.Content)
                imageState = ImageState.OldImage
                //Set background color
                binding.root.background = ResourcesCompat.getDrawable(
                    resources,
                    NoteColorConverter.enumToColor(editedNote.Color),
                    null
                )
            }
        }
    }

    /** Register a request to start an activity for result for getting image from gallery */
    private val galleryStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageState = ImageState.NewGalleryImage
            //Handle loaded image from gallery
            result.data?.data?.let {
                galleryImage =
                    if (Build.VERSION.SDK_INT < 28)
                        MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                    else
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, it)).copy(Bitmap.Config.ARGB_8888, true)
            }
            galleryImage?.let { setImage(it) }
        }
    }

    /** Register a request to start an activity for result for getting image from camera */
    private val cameraStartForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageState = ImageState.NewCameraImage
            cameraImage?.path?.let{ setImage(it)}
        }
    }

    /** Create [File] for image with name containing current time stamp */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        //Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "image_${timeStamp}.jpg").apply {
            createNewFile()
        }
    }

    /** Save image into db */
    private fun saveImageToDB(newImage: File){
        when{
            dataID != -1 -> {
                //update
                GlobalScope.launch {
                    db.dataDao().update(db.dataDao().getDataById(dataID).apply { Content = newImage.path; Info = null })
                    db.noteDao().update(db.noteDao().getNoteById(noteID).apply { Date = Date()})
                }
            }
            noteID != -1 -> {
                //add new data to db
                GlobalScope.launch {
                    db.dataDao().insertAll(Data(0, noteID, NoteType.Image, newImage.path,null,null,null))
                    db.noteDao().update(db.noteDao().getNoteById(noteID).apply { Date = Date()})
                }
            }
            else -> {
                //create intent for note viewer
                val noteViewerActivityIntent = Intent(this, NoteViewerActivity::class.java)
                //create new Note and Data
                GlobalScope.launch {
                    val newNoteID = db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White))
                    val newDataID = db.dataDao().insertAll(Data(0, newNoteID[0].toInt(), NoteType.Image,newImage.path,null,null,null))
                    db.noteDao().update(db.noteDao().getNoteById(newNoteID[0].toInt()).apply { MainData = newDataID[0].toInt() })
                    //open note
                    noteViewerActivityIntent.run {
                        putExtra("noteID", newNoteID[0].toInt())
                        startActivity(this)
                    }
                }
            }
        }
    }

    /** Set image from given [path]. */
    private fun setImage(path: String){
        Glide.with(thisActivity)
            .load(path)
            .fitCenter()
            .placeholder(R.drawable.ic_loading)
            .into(binding.chosenImage)
    }

    /** Set image from given [Bitmap]. */
    private fun setImage(bitmap: Bitmap){
        Glide.with(thisActivity)
            .load(bitmap)
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
