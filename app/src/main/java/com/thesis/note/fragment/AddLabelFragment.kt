package com.thesis.note.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thesis.note.databinding.DialogFragmentAddLabelBinding

/**
 *  Fragment for adding new label
 *  After confirm it sets fragment result with resultKey 'newLabel'
 *  and bundle new label as [String] 'name'
 */
class AddLabelFragment :DialogFragment(){

    /** View binding */
    private lateinit var binding: DialogFragmentAddLabelBinding

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentAddLabelBinding.inflate(requireActivity().layoutInflater)

        binding.saveGroupButton.setOnClickListener {
            setFragmentResult("newLabel", Bundle().apply { putString("name", binding.newLabelName.text.toString()) })
            dismiss()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
