package com.thesis.note.fragment

import android.media.MediaPlayer
import android.os.Bundle
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
import com.thesis.note.databinding.FragmentSoundPlayerBinding
import java.io.FileInputStream
import java.io.IOException

class SoundPlayer : Fragment(R.layout.fragment_sound_player) {
    /** */
    class SoundPlayerViewModel : ViewModel() {
        private val mutableSelectedItem = MutableLiveData<String>()
        val selectedItem: LiveData<String> get() = mutableSelectedItem

        fun setItem(item: String) {
            mutableSelectedItem.value = item
        }
    }

    /** */
    private val viewModel: SoundPlayerViewModel by activityViewModels()

    /** View binding */
    lateinit var binding: FragmentSoundPlayerBinding

    /** [MediaPlayer] */
    private var mediaPlayer: MediaPlayer? = null

    lateinit var handler : Handler


    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            binding.timeNow.text = toTime(mediaPlayer?.currentPosition)
            handler.postDelayed(this, 1000)
        }
    }

    /** On create view callback */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentSoundPlayerBinding.inflate(layoutInflater)

        viewModel.selectedItem.observe(viewLifecycleOwner, { item ->
            setPlayer(item)
        })

        handler = Handler(Looper.getMainLooper())

        binding.playButton.isEnabled = false



        binding.playButton.setOnClickListener {
            mediaPlayer?.start()
            handler.apply {
                post(runnableCode)
            }

            Toast.makeText(context, "play", Toast.LENGTH_SHORT).show()
        }
        binding.pauseButton.setOnClickListener {
            mediaPlayer?.pause()
            handler.removeCallbacks(runnableCode)
            Toast.makeText(context, "pause", Toast.LENGTH_SHORT).show()
        }
        binding.stopButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            binding.timeNow.text =  getString(R.string.fragment_sound_player_time_zero)
            Toast.makeText(context, "stop", Toast.LENGTH_SHORT).show()
            handler.removeCallbacks(runnableCode)
        }

        return binding.root
    }

    /** Open file into [mediaPlayer] */
    private fun setPlayer (filePath : String) {
        mediaPlayer?.stop()

        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                val fis =  FileInputStream(filePath)
                setDataSource(fis.fd)
                prepare()
                binding.playButton.isEnabled = true
                setOnCompletionListener {
                    handler.removeCallbacks(runnableCode)
                    handler.postDelayed(runnableCode,1000)
                }
            } catch (e: IOException) {
                Toast.makeText(context,  "ERROR", Toast.LENGTH_SHORT).show()
            }

    }
        binding.allTime.text = toTime(mediaPlayer?.duration)
    }

    /** */
    private fun toTime(milliseconds: Int?) : String{
        return if(milliseconds == -1 || milliseconds == null)
            getString(R.string.fragment_sound_player_time_zero)
        else
            (milliseconds/60000).toString()+":"+(milliseconds/1000%60).let{if(it<10) "0"+it.toString() else it.toString()}
    }

}