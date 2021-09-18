package com.thesis.note

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.widget.TextView
import java.io.FileInputStream
import java.io.IOException

/**
 *
 */
class SoundPlayer(val context: Context) {

    /** [MediaPlayer] */
    private var mediaPlayer: MediaPlayer? = null

    /** */
    fun isFileOpen():Boolean{
        return mediaPlayer!= null
    }

    /** Path of open file */
    var filePath : String? = null
        private set

    /** Open file from [path]. Returns true if succeed */
    fun openFile(path:String): Boolean{
        //release current mediaPlayer
        mediaPlayer?.release()
        mediaPlayer = null
        //start
        mediaPlayer = MediaPlayer().apply {
            try {
                reset()
                val fis = FileInputStream(path)
                setDataSource(fis.fd)
                prepare()
                setOnCompletionListener {
                    onEndPlayingListener.invoke()
                    handler?.removeCallbacks(timeCounter)
                    currentPositionTextView?.text = toMinutesAndSeconds(mediaPlayer?.currentPosition)
                }
                durationTextView?.text = toMinutesAndSeconds(duration)
                filePath = path
            } catch (e: IOException) {
                release()
                mediaPlayer = null
            }
        }
        return mediaPlayer != null
    }

    /** Close currently opened file */
    fun closeFile(){
        mediaPlayer?.release()
        mediaPlayer = null
        filePath = null
        currentPositionTextView?.text = context.getString(R.string.sound_player_time_zero)
        durationTextView?.text = context.getString(R.string.sound_player_time_zero)
    }

    /** Start playing */
    fun play(){
        mediaPlayer?.also {
            it.start()
            handler?.post(timeCounter)
            onStartPlayingListener.invoke()
        }
    }

    /** Pause playing */
    fun pause(){
        if(mediaPlayer?.isPlaying == true)
            mediaPlayer?.also{
                it.pause()
                handler?.removeCallbacks(timeCounter)
                onPausePlayingListener.invoke()
            }
    }

    /** Stop playing */
    fun stop(){
        mediaPlayer?.also {
            it.stop()
            it.prepare()
            handler?.removeCallbacks(timeCounter)
            currentPositionTextView?.text = toMinutesAndSeconds(0)
            onEndPlayingListener.invoke()
        }
    }

    /** Release [SoundPlayer] */
    fun release(){
        mediaPlayer?.release()
    }

    /** [TextView] for displaying current playback position. You must set [handler] for it to work.*/
    var currentPositionTextView : TextView? = null

    /** [TextView] for displaying duration of the file. */
    var durationTextView : TextView? = null
        set(value){
            field = value
            value?.text = toMinutesAndSeconds(mediaPlayer?.duration)
        }

    /** Convert milliseconds to minutes and seconds.*/
    private fun toMinutesAndSeconds(milliseconds: Int?) : String{
        return if(milliseconds == -1 || milliseconds == null)
            context.getString(R.string.sound_player_time_zero)
        else
            (milliseconds/60000).toString()+":"+(milliseconds/1000%60).let{if(it<10) "0$it" else it.toString()}
    }

    /** Handler for showing current playback position. */
    var handler : Handler? = null

    /** [Runnable] with function to update current playback position. */
    private val timeCounter: Runnable = object : Runnable {
        override fun run() {
            currentPositionTextView?.text = toMinutesAndSeconds(mediaPlayer?.currentPosition)
            handler?.postDelayed(this, 1000)
        }
    }

    /** Listener called when [SoundPlayer] started playing */
    var onStartPlayingListener : () -> Unit = {}

    /** Listener called when [SoundPlayer] paused playing */
    var onEndPlayingListener : () -> Unit = {}

    /** Listener called when [SoundPlayer] stopped playing */
    var onPausePlayingListener : () -> Unit = {}
}