package com.thesis.note.activity

import android.Manifest
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer
import com.thesis.note.R
import com.google.android.material.navigation.NavigationView

import kotlinx.android.synthetic.main.activity_voice_to_text.*

import android.view.View
import android.speech.RecognizerIntent
import android.content.Intent

import android.speech.RecognitionListener

import android.util.Log

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

//TODO
@Deprecated("old")
class VoiceToTextActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,View.OnClickListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer


    lateinit var voiceRecognitor : SpeechRecognizer

    lateinit var textViewA : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setSupportActionBar(toolbar)
        setContentView(R.layout.activity_voice_to_text)      //NAZWA LAYOUTU
        drawer_layout = activity_voice_to_text_layout;               //NAZWA DRAWER LAYOUTU
        navigationDrawer = NavigationDrawer(drawer_layout)
        navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------

        textViewA = textView2
        startRecordButton.setOnClickListener(this)

        voiceRecognitor = SpeechRecognizer.createSpeechRecognizer(this)
        voiceRecognitor.setRecognitionListener(listener())




        this.runOnUiThread {
          //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestRecordAudioPermission()
           // }
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
    override fun onClick(v: View) {
            requestRecordAudioPermission()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            //intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, application.packageName)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.example.note.activity")
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            voiceRecognitor.startListening(intent)
            //Log.i("111111", "11111111")
        }

    internal inner class listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Log.d("VoiceListener", "onReadyForSpeech")

        }

        override fun onBeginningOfSpeech() {
            Log.d("VoiceListener", "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d("VoiceListener", "onRmsChanged")
        }

        override fun onBufferReceived(buffer: ByteArray) {
            Log.d("VoiceListener", "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.d("VoiceListener", "onEndofSpeech")
        }

        override fun onError(error: Int) {
            Log.d("VoiceListener", "error $error")
            textViewA.setText("error $error")
        }

        override fun onResults(results: Bundle) {
            var str = String()
            Log.d("VoiceListener", "onResults $results")
            val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            for (i in 0 until data!!.size) {
                Log.d("VoiceListener", "result " + data[i])
                str += data[i]
            }
            textViewA.setText("results: " + str)
        }

        override fun onPartialResults(partialResults: Bundle) {
            Log.d("VoiceListener", "onPartialResults")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            Log.d("VoiceListener", "onEvent $eventType")
        }
    }

    private fun requestRecordAudioPermission() {
       // var perm = arrayOf<String>(Manifest.permission.RECORD_AUDIO)
        var perm = arrayOf<String>(Manifest.permission.RECORD_AUDIO)

        var MY_PERMISSIONS_RECORD_AUDIO:Int  = 1;
        var thisActivity = this;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {




            ActivityCompat.requestPermissions(thisActivity,perm,MY_PERMISSIONS_RECORD_AUDIO);
        }
    }


}
