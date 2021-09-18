package com.thesis.note

import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.widget.TextView
import java.io.IOException
//TODO documentation
/**
 *
 *
 */
class SoundRecorder(val context: Context) {

    /** MediaRecorder */
    private var mediaRecorder: MediaRecorder? = null

    /** */
    var isWorking: Boolean = false
        private set

    /** */
    var filePath: String? = null

    /** */
    fun startRecording() {
        mediaRecorder?.let {
            it.reset()
            duration = 0
            durationTextView?.text = toMinutesAndSeconds(0)
            handler?.removeCallbacks(timeCounter)
        }
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(filePath)
            try {
                prepare()
                start()
                duration = 0
                durationTextView?.text = toMinutesAndSeconds(0)
                handler?.postDelayed(timeCounter,1000)
                isWorking = true
                onStartRecordingListener.invoke()
            } catch (e: IOException) { }
        }
    }

    /** */
    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            handler?.removeCallbacks(timeCounter)
            durationTextView?.text = toMinutesAndSeconds(duration)
            isWorking = false
            onStopRecordingListener.invoke(filePath)
        }
        mediaRecorder = null
    }

    /** */
    fun cancelRecording(){
        mediaRecorder?.apply {
            reset()
            release()
            duration = 0
            durationTextView?.text = toMinutesAndSeconds(0)
            handler?.removeCallbacks(timeCounter)
            isWorking = false
        }
        onCancelRecordingListener.invoke()
        mediaRecorder = null
    }

    /** */
    fun release(){
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        handler?.removeCallbacks(timeCounter)
    }

    /** Duration or recording */
    private var duration = 0

    /** Convert milliseconds to minutes and seconds.*/
    private fun toMinutesAndSeconds(milliseconds: Int?) : String{
        return if(milliseconds == -1 || milliseconds == null)
            context.getString(R.string.sound_player_time_zero)
        else
            (milliseconds/60000).toString()+":"+(milliseconds/1000%60).let{if(it<10) "0$it" else it.toString()}
    }

    /** [TextView] for displaying duration of the file. */
    var durationTextView : TextView? = null
        set(value){
            field = value
            value?.text = toMinutesAndSeconds(duration)
        }

    /** Handler for showing current playback position. */
    var handler : Handler? = null

    /** Time counter */
    private val timeCounter: Runnable = object : Runnable {
        override fun run() {
            duration += 1000
            durationTextView?.text = toMinutesAndSeconds(duration)
            handler?.postDelayed(this, 1000)
        }
    }

    /** Listener called when  */
    var onStartRecordingListener : () -> Unit = {}

    /** Listener called when  */
    var onStopRecordingListener : (String?) -> Unit = {}

    /** Listener called when  */
    var onCancelRecordingListener : () -> Unit = {}
}