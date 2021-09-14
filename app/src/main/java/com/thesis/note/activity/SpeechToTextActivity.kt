package com.thesis.note.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.google.android.material.navigation.NavigationView
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.databinding.ActivitySpeechToTextBinding
import java.util.*

@Deprecated("add to text notes")
class SpeechToTextActivity : DrawerActivity() {

    private lateinit var binding: ActivitySpeechToTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpeechToTextBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        //------------------------------------------------------------------------------------------
        binding.button.setOnClickListener {
            val speachToText = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speachToText.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speachToText.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            speachToText.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
            try {
                startForResult.launch(speachToText)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "error", Toast.LENGTH_LONG).show()
            }
        }
    }


    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultArray = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = resultArray?.get(0)
            binding.textView6.text = recognizedText
        }
    }
}
