package com.thesis.note.fragment

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.thesis.note.R
import com.thesis.note.activity.ImageNoteActivity
import com.thesis.note.activity.SoundEditorActivity
import com.thesis.note.activity.TextEditorActivity
import com.thesis.note.databinding.DialogFragmentAddNoteBinding

/**
 *  Fragment for adding new notes.
 *  It checks if all needed permissions are granted
 *  and if not it asks user for them
 */
class AddNoteFragment:DialogFragment(), ActivityCompat.OnRequestPermissionsResultCallback{

    /** View binding */
    private lateinit var binding: DialogFragmentAddNoteBinding

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentAddNoteBinding.inflate(requireActivity().layoutInflater)

        binding.addTextNote.setOnClickListener{
            startActivity(Intent(requireContext(), TextEditorActivity::class.java).apply{
                putExtra("noteID", -1)
                putExtra("dataID", -1)
            })
            dismiss()
        }
        binding.addImageNote.setOnClickListener {
            checkPermissionsAndLaunchImageNote()
        }
        binding.addSoundNote.setOnClickListener {
            checkPermissionsAndLaunchSoundNote()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    /** Check if all permissions for image note is granted
     * and if not ask user for them.
     * If permissions are granted it launches new image note */
    private fun checkPermissionsAndLaunchImageNote() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            requestPermissionsImageNote.launch(permissions)
        }
        else
        {
            launchImageNote()
        }
    }

    /** Launch empty [ImageNoteActivity] and close current [AddNoteFragment] */
    private fun launchImageNote(){
        startActivity(Intent(this.context, ImageNoteActivity::class.java).apply{
            putExtra("noteID", -1)
            putExtra("dataID", -1)
        })
        dismiss()
    }

    /** Asks user for permissions needed for image note.
     * If user accept it launches new image note
     * and if not it show [Toast] with information */
    private val requestPermissionsImageNote = registerForActivityResult(RequestMultiplePermissions()
    ) { permissions ->
        var isGranted = false
        permissions.forEach { isGranted = it.value }
        if(isGranted)
            launchImageNote()
        else
            Toast.makeText(this.context, R.string.fragment_add_note_no_permissions, Toast.LENGTH_SHORT).show()
    }

    /** Check if all permissions for sound note is granted
     * and if not ask user for them.
     * If permissions are granted it launches new sound note */
    private fun checkPermissionsAndLaunchSoundNote() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionsSoundNote.launch(Manifest.permission.RECORD_AUDIO)
        }
        else{
            launchSoundNote()
        }
    }

    /** Launch empty [SoundEditorActivity] and close current [AddNoteFragment] */
    private fun launchSoundNote(){
        startActivity(Intent(this.context, SoundEditorActivity::class.java).apply{
            putExtra("noteID", -1)
            putExtra("dataID", -1)
        })
        dismiss()
    }

    /** Asks user for permissions needed for sound note.
     * If user accept it launches new sound note
     * and if not it show [Toast] with information */
    private val requestPermissionsSoundNote = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(isGranted)
            launchSoundNote()
        else
            Toast.makeText(this.context, R.string.fragment_add_note_no_permissions, Toast.LENGTH_SHORT).show()
    }

}
