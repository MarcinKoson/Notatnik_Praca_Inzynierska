package com.thesis.note.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.thesis.note.databinding.DialogFragmentAboutBinding

/**
 *  Fragment for information about app
 */
class AboutFragment :DialogFragment(){

    /** View binding */
    private lateinit var binding: DialogFragmentAboutBinding

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentAboutBinding.inflate(requireActivity().layoutInflater)

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
