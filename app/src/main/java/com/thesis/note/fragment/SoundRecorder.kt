package com.thesis.note.fragment

import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thesis.note.R
import com.thesis.note.databinding.FragmentSoundRecorderBinding
import com.thesis.note.fragment.SoundRecorder.SoundRecorderViewModel
import java.io.File
import java.io.IOException

/**
 * [Fragment] for recording sound.
 * You can communicate with it by [SoundRecorderViewModel].
 */

class SoundRecorder : Fragment(R.layout.fragment_sound_recorder) {
    /** [ViewModel] for this [Fragment]*/
    class SoundRecorderViewModel : ViewModel() {
        /** Info if [SoundRecorder] is currently working */
        private val mutableIsWorking = MutableLiveData<Boolean>()
        /** Info if [SoundRecorder] is currently working */
        val isWorking: LiveData<Boolean> get() = mutableIsWorking
        /** Set if [SoundRecorder] is working */
        fun setIsWorking(newValue: Boolean){ mutableIsWorking.value = newValue }

        /** Info if GUI of [SoundRecorder] is enabled */
        private val mutableIsEnabled = MutableLiveData<Boolean>()
        /** Info if GUI of [SoundRecorder] is enabled */
        val isEnabled: LiveData<Boolean> get() = mutableIsEnabled
        /** Enable or disable GUI of [SoundRecorder] */
        fun setIsEnabled(newValue: Boolean){ mutableIsEnabled.value = newValue }

        /** Output file */
        private val mutableOutputFile = MutableLiveData<File>()
        /** Output file */
        val outputFile: LiveData<File> get() = mutableOutputFile
        /** Set output file */
        fun setOutputFile(file: File){ mutableOutputFile.value = file }

        /**  */
        private val mutableOnRecordingEndListener = MutableLiveData<(String) -> Unit>()
        /**  */
        val onRecordingEndListener: LiveData<(String) -> Unit> get() = mutableOnRecordingEndListener
        /**  */
        fun setOnRecordingEndListener(newValue: (String) -> Unit){ mutableOnRecordingEndListener.value = newValue }

        /**  */
        private val mutableOnRecordingCancelListener = MutableLiveData<() -> Unit>()
        /**  */
        val onRecordingCancelListener: LiveData<() -> Unit> get() = mutableOnRecordingCancelListener
        /**  */
        fun setOnRecordingCancelListener(newValue: () -> Unit){ mutableOnRecordingCancelListener.value = newValue }
    }

    /** View Model */
    private val viewModel: SoundRecorderViewModel by activityViewModels()

    /** View Binding */
    lateinit var binding: FragmentSoundRecorderBinding

    /** MediaRecorder */
    private var mediaRecorder: MediaRecorder? = null

    /** [Handler] for [timeCounter]  */
    lateinit var handler : Handler

    /** Time counter */
    private val timeCounter: Runnable = object : Runnable {
        override fun run() {
            duration += 1000
            binding.timeNow.text = toTime(duration)
            handler.postDelayed(this, 1000)
        }
    }

    /** Duration or recording */
    var duration = 0

    /** On create view callback */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSoundRecorderBinding.inflate(layoutInflater)
        //set handler
        handler = Handler(Looper.getMainLooper())

        //Observer for isEnabled
        viewModel.isEnabled.observe(viewLifecycleOwner, { setIsEnabled(it) })

        //Record button listener
        binding.recordButton.setOnClickListener {
            if(mediaRecorder == null)
                startRecording()
            else
                stopRecording()
        }

        //Cancel button listener
        binding.cancelButton.setOnClickListener {
            cancelRecording()
        }

        return binding.root
    }

    /** On pause callback */
    override fun onPause() {
        super.onPause()
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        handler.removeCallbacks(timeCounter)
        viewModel.setIsWorking(false)
        binding.recordButton.setBackgroundResource(R.drawable.ic_baseline_fiber_manual_record)
    }

    /** */
    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(viewModel.outputFile.value?.path)
            try {
                prepare()
                start()
                viewModel.setIsWorking(true)
                binding.recordButton.setBackgroundResource(R.drawable.ic_baseline_check)
                duration = -1000
                handler.post(timeCounter)
            } catch (e: IOException) { }
        }
    }

    /** */
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            viewModel.onRecordingEndListener.value?.invoke(viewModel.outputFile.value?.path?:"")
        }
        binding.recordButton.setBackgroundResource(R.drawable.ic_baseline_fiber_manual_record)
        viewModel.setIsWorking(false)
        mediaRecorder = null
        handler.removeCallbacks(timeCounter)
    }

    /** */
    private fun cancelRecording(){
        mediaRecorder?.apply {
            reset()
            release()
        }
        mediaRecorder = null
        duration = 0
        binding.timeNow.text = getString(R.string.sound_player_time_zero)
        binding.recordButton.setBackgroundResource(R.drawable.ic_baseline_fiber_manual_record)
        viewModel.setIsWorking(false)
        handler.removeCallbacks(timeCounter)
        viewModel.onRecordingCancelListener.value?.invoke()
    }

    /** Convert milliseconds to minutes and seconds*/
    private fun toTime(milliseconds: Int?) : String{
        return if(milliseconds == -1 || milliseconds == null)
            getString(R.string.sound_player_time_zero)
        else
            (milliseconds/60000).toString()+":"+(milliseconds/1000%60).let{if(it<10) "0$it" else it.toString()}
    }

    /** Enable or disable GUI of [SoundRecorder] */
    private fun setIsEnabled(value: Boolean){
        binding.recordButton.isEnabled = value
        binding.pauseButton.isEnabled = value
        binding.cancelButton.isEnabled = value
    }
}