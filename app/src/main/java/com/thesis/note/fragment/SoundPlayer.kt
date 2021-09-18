package com.thesis.note.fragment

import android.media.MediaPlayer
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
import com.thesis.note.databinding.FragmentSoundPlayerBinding
import com.thesis.note.fragment.SoundPlayer.SoundPlayerViewModel
import java.io.FileInputStream
import java.io.IOException

//TODO multiple instances working
/**
 * [Fragment] for playing music.
 * You can communicate with it by [SoundPlayerViewModel].
 */
@Deprecated("deprecated")
class SoundPlayer : Fragment(R.layout.fragment_sound_player) {

    /** [ViewModel] for this [Fragment]*/
    class SoundPlayerViewModel : ViewModel() {
        /** Info if [mediaPlayer] is currently working */
        private val mutableIsWorking = MutableLiveData<Boolean>()
        /** Info if [SoundPlayer] is currently working */
        val isWorking: LiveData<Boolean> get() = mutableIsWorking
        /** Set if [SoundPlayer] is working */
        fun setIsWorking(newValue: Boolean){ mutableIsWorking.value = newValue }

        /** Info if GUI of [SoundPlayer] is enabled */
        private val mutableIsEnabled = MutableLiveData<Boolean>()
        /** Info if GUI of [SoundPlayer] is enabled */
        val isEnabled: LiveData<Boolean> get() = mutableIsEnabled
        /** Enable or disable GUI of [SoundPlayer] */
        fun setIsEnabled(newValue: Boolean){ mutableIsEnabled.value = newValue }

        /** Path to current loaded file */
        private val mutableFilePath = MutableLiveData<String>()
        /** Path to current loaded file */
        val filePath: LiveData<String> get() = mutableFilePath
        /** Open file in [SoundPlayer] */
        fun setFilePath(filePath: String) { mutableFilePath.value = filePath }
    }

    /** View model */
    private val viewModel: SoundPlayerViewModel by activityViewModels()

    /** View binding */
    lateinit var binding: FragmentSoundPlayerBinding

    /** [MediaPlayer] */
    private var mediaPlayer: MediaPlayer? = null

    /** [Handler] for [timeCounter]  */
    lateinit var handler : Handler

    /** Time counter */
    private val timeCounter: Runnable = object : Runnable {
        override fun run() {
            binding.timeNow.text = toTime(mediaPlayer?.currentPosition)
            handler.postDelayed(this, 1000)
        }
    }

    /** On create view callback */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSoundPlayerBinding.inflate(layoutInflater)
        //Observer for isEnabled
        viewModel.isEnabled.observe(viewLifecycleOwner, { setIsEnabled(it) })
        //Observer for filePath
        viewModel.filePath.observe(viewLifecycleOwner, { setPlayer(it) })
        //set handler
        handler = Handler(Looper.getMainLooper())
        //Play button listener
        binding.playButton.setOnClickListener { play() }
        //Pause button listener
        binding.pauseButton.setOnClickListener { pause() }
        //Stop button listener
        binding.stopButton.setOnClickListener { stop() }

        return binding.root
    }

    /** On pause callback */
    override fun onPause() {
        super.onPause()
        mediaPlayer?.apply {
            stop()
            release()
        }
    }

    /** Open file into [mediaPlayer] */
    private fun setPlayer (filePath : String) {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                val fis =  FileInputStream(filePath)
                setDataSource(fis.fd)
                prepare()
                setOnCompletionListener {
                    handler.removeCallbacks(timeCounter)
                    binding.timeNow.text = toTime(mediaPlayer?.currentPosition)
                    viewModel.setIsWorking(false)
                }
            } catch (e: IOException) {
                release()
                mediaPlayer = null
                binding.playButton.isEnabled = false
                binding.allTime.text = getString(R.string.sound_player_time_zero)
            }

        }
        if(mediaPlayer != null){
            binding.allTime.text = toTime(mediaPlayer?.duration)
            setIsEnabled(viewModel.isEnabled.value?:false)
        }

    }

    /** Play current loaded file */
    private fun play(){
        mediaPlayer?.also {
            it.start()
            onStartPlayingListener.invoke()
            viewModel.setIsWorking(true)
            handler.post(timeCounter)
        }
    }

    /** Pause playing */
    private fun pause(){
        mediaPlayer?.also{
            it.pause()
            onPausePlayingListener.invoke()
            viewModel.setIsWorking(false)
            handler.removeCallbacks(timeCounter)
        }
    }

    /** Stop playing */
    private fun stop(){
        mediaPlayer?.also {
            it.stop()
            it.prepare()
            onEndPlayingListener.invoke()
            viewModel.setIsWorking(false)
            handler.removeCallbacks(timeCounter)
            binding.timeNow.text =  getString(R.string.sound_player_time_zero)
        }
    }

    /** Convert milliseconds to minutes and seconds*/
    private fun toTime(milliseconds: Int?) : String{
        return if(milliseconds == -1 || milliseconds == null)
            getString(R.string.sound_player_time_zero)
        else
            (milliseconds/60000).toString()+":"+(milliseconds/1000%60).let{if(it<10) "0$it" else it.toString()}
    }

    /** Enable or disable GUI of [SoundPlayer] */
    public fun setIsEnabled(value:Boolean){
        if(mediaPlayer == null)
            binding.playButton.isEnabled = false
        else
            binding.playButton.isEnabled = value
        binding.pauseButton.isEnabled = value
        binding.stopButton.isEnabled = value
        if (!value){
            binding.timeNow.text = getString(R.string.sound_player_time_zero)
            binding.allTime.text = getString(R.string.sound_player_time_zero)
        }
        else{
            mediaPlayer?.let {
                binding.timeNow.text = toTime(it.currentPosition)
                binding.allTime.text = toTime(it.duration)
            }
        }
    }

    public fun setNewFilePath(path: String){
        viewModel.setFilePath(path)
    }

    public var onStartPlayingListener : () -> Unit = {}

    public var onEndPlayingListener : () -> Unit = {}

    public var onPausePlayingListener : () -> Unit = {}

}