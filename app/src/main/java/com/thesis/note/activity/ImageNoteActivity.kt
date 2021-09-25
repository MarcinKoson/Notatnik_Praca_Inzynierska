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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
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
 * When creating [Intent] of this activity, you can put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * Activity will load [Note] and [Data] with passed id.
 * If passed id equals "0" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "0".
 *
 */
class ImageNoteActivity : DrawerActivity()
{
    /** This activity */
    private val thisActivity = this

    /** View binding */
    private lateinit var binding: ActivityImageNoteBinding

    /** Database */
    private lateinit var db: AppDatabase

    /** Edited [Note]  id */
    private var noteID:Int = 0

    /** Edited [Note] */
    private var editedNote: Note? = null

    /** Edited [Data] id */
    private var dataID:Int = 0

    /** Edited [Data] */
    private var editedData: Data? = null

    /** Image from camera */
    private var cameraImage : File? = null

    /** Image from gallery */
    private var galleryImage : Bitmap? = null

    /** State of current loaded image */
    private var imageState = ImageState.NoImage

    /** On create callback. Loading data, layout init and setting listeners. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageNoteBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        //open database
        db = AppDatabase(this)
        loadParameters()
        GlobalScope.launch {
            loadFromDB()
            runOnUiThread{
                if(dataID != 0) {
                    setImage(editedData!!.Content)
                    imageState = ImageState.OldImage
                }
                if(noteID != 0){
                    //Set background color
                    binding.root.background = ResourcesCompat.getDrawable(
                        resources,
                        NoteColorConverter.enumToColor(editedNote?.Color),
                        null
                    )
                }
            }
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

        //Delete button listener
        binding.deleteButton.setOnClickListener {
            AlertDialog.Builder(thisActivity).run{
                setPositiveButton(R.string.activity_image_note_dialog_remove_note_positive_button) { _, _ ->
                    if (dataID != 0){
                        GlobalScope.launch {
                            if (editedNote?.MainData == dataID){
                                with(db.dataDao().getDataFromNote(noteID).map { it.IdData }.toMutableList()){
                                    remove(dataID)
                                    if(size == 0)
                                        editedNote!!.MainData = null
                                    else
                                        editedNote!!.MainData = this[0]
                                    db.noteDao().update(editedNote!!)
                                }
                            }
                            editedData?.let { it1 -> db.dataDao().delete(it1) }
                            try{ File(editedData?.Content?:"").delete() }catch(ex:Exception){}
                        }
                    }
                    finish()
                }
                setNegativeButton(R.string.activity_image_note_dialog_remove_note_negative_button) { _, _ -> }
                setTitle(R.string.activity_image_note_dialog_remove_note)
                create()
            }.show()
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
                        putExtra(Intent.EXTRA_STREAM,getUriForFile(thisActivity,"com.thesis.note.fileprovider", File(editedData?.Content?:"")))
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

    /** Load [Data] and [Note] form database. */
    private fun loadFromDB(){
        if (noteID != 0) {
            editedNote = db.noteDao().getNoteById(noteID)
        }
        if (dataID != 0) {
            editedData = db.dataDao().getDataById(dataID)

            if (noteID == 0) {
                noteID = editedData?.NoteId?:0
                editedNote = db.noteDao().getNoteById(noteID)
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
        else{
            Toast.makeText(applicationContext, R.string.activity_image_note_no_image, Toast.LENGTH_SHORT).show()
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
            dataID != 0 -> {
                //update
                GlobalScope.launch {
                    db.dataDao().update(db.dataDao().getDataById(dataID).apply { Content = newImage.path; Info = null })
                    editedNote?.apply { Date = Date()}?.let { db.noteDao().update(it) }
                }
            }
            noteID != 0 -> {
                //add new data to db
                GlobalScope.launch {
                    val addedData = db.dataDao().insertAll(Data(0, noteID, NoteType.Image, newImage.path,null,null,null))
                    editedNote?.apply { Date = Date(); if(MainData==null) MainData=addedData[0].toInt()}?.let {
                        db.noteDao().update(it)
                    }
                }
            }
            else -> {
                //create new Note and Data
                GlobalScope.launch {
                    db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White)).also{
                        noteID = it[0].toInt()
                    }
                    db.dataDao().insertAll(Data(0, noteID, NoteType.Image,newImage.path,null,null,null)).also{
                        dataID = it[0].toInt()
                        db.noteDao().update(db.noteDao().getNoteById(noteID).apply{ MainData = dataID })
                    }
                    //open new note
                    runOnUiThread {
                        thisActivity.startActivity(Intent(thisActivity, NoteViewerActivity::class.java).apply{putExtra("noteID", noteID)})
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
