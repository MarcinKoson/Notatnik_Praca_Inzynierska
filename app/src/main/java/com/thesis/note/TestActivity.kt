package com.thesis.note

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.thesis.note.databinding.DebugActivityBinding
import com.thesis.note.databinding.TestActivityBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class TestActivity : AppCompatActivity() {

    private lateinit var binding:TestActivityBinding

    private lateinit var contextThis : Context

    private var imageView: ImageView? = null
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            imageView?.setImageBitmap(imageBitmap)



            //  var nUri = imageUri?.path
            //   nUri = nUri?.drop(6)
            //   val imageFile = File(nUri)


            // imageBitmap
//TUTAJ

/*
            val currenttime = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
            val fileNameDate = "/image" + currenttime.format(Date())
            val fileNameToCopy = File(sth, fileNameDate)
            fileNameToCopy.createNewFile()


            GlobalScope.launch {
            val outStream = FileOutputStream(fileNameToCopy)
            val kompre = imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, outStream)

            outStream.flush()
            outStream.close()
runOnUiThread {
                binding.textView2.text = kompre.toString()}
        }
*/

         //   binding.debugImageNote.text = fileNameToCopy.path
           // val newFile = imageFile?.copyTo(fileNameToCopy, true)

        }}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TestActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contextThis = this.applicationContext
        imageView = binding.imageView2

        binding.appaarat.setOnClickListener {
            dispatchTakePictureIntent()



        }



    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {

        val takePictureIntent =
        try {
           // startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
               val inttus = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val sth = contextThis.applicationContext. getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            ///var nUri = imageUri?.path
           // //nUri = nUri?.drop(6)
        //    /val imageFile = File(nUri)


            val currenttime = SimpleDateFormat("yyyy.MM.dd-HH:mm:ss")
            val fileNameDate = "/image" + currenttime.format(Date())
            val fileNameToCopy = File(sth, fileNameDate)
            fileNameToCopy.createNewFile()

          //  var nurisd = fileNameToCopy.toUri().path

         //   nurisd= nurisd?.drop(6)


            inttus.  putExtra(MediaStore.EXTRA_OUTPUT,
                fileNameToCopy.toUri()
                ///Uri.parse(nurisd)
                )

            startForResult.launch(inttus)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }









}

    lateinit var currentPhotoPath: String

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }



}




