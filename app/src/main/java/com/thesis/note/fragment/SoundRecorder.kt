package com.thesis.note.fragment

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thesis.note.R
import com.thesis.note.databinding.FragmentSoundRecorderBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SoundRecorder : Fragment(R.layout.fragment_sound_recorder) {

    lateinit var binding: FragmentSoundRecorderBinding

    private var filePath: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        handler = Handler(Looper.getMainLooper())

        binding = FragmentSoundRecorderBinding.inflate(layoutInflater)
        //create new file
        val timeStamp: String =
            SimpleDateFormat("yyyy.MM.dd-HH:mm:ss", Locale.US).format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val newFile = File.createTempFile("audio_${timeStamp}_", ".amr", storageDir)
        newFile.createNewFile()
        filePath = newFile.path

        binding.recordButton.isEnabled = true

        viewModel.setItem(filePath)

        binding.recordButton.setOnClickListener {
            startRecording()
            Toast.makeText(context, "start", Toast.LENGTH_SHORT).show()
        }
        binding.cancelButton.setOnClickListener {
            stopRecording()
            Toast.makeText(context, "stop", Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }



    private val viewModel: SoundRecorderViewModel by activityViewModels()

    class SoundRecorderViewModel : ViewModel() {

        val listener = MutableLiveData< (String) -> Unit >()


        private val mutableSelectedItem = MutableLiveData<String>()
        val selectedItem: LiveData<String> get() = mutableSelectedItem

        fun setItem(item: String) {
            mutableSelectedItem.value = item
        }
    }

    lateinit var handler : Handler

    var duration = 0

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            //binding.timeNow.text = //toTime(mediaRecorder?.)
            duration += 1000
            binding.timeNow.text = toTime(duration)
            handler.postDelayed(this, 1000)
        }
    }

    private var mediaRecorder: MediaRecorder? = null

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        duration = -1000
        handler.post(runnableCode)

    }
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        handler.removeCallbacks(runnableCode)
        viewModel.listener.value?.invoke((filePath))

    }
    /** */
    private fun toTime(milliseconds: Int?) : String{
        return if(milliseconds == -1 || milliseconds == null)
            getString(R.string.fragment_sound_player_time_zero)
        else
            (milliseconds/60000).toString()+":"+(milliseconds/1000%60).let{if(it<10) "0"+it.toString() else it.toString()}
    }
}