package com.thesis.note.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thesis.note.databinding.DialogChooseColorBinding
//TODO documentation
class ChooseColorFragment:DialogFragment(){
    private lateinit var binding: DialogChooseColorBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChooseColorBinding.inflate(requireActivity().layoutInflater)

        binding.color0.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "0"))
            dismiss()
        }
        binding.color1.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "1"))
            dismiss()
        }
        binding.color2.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "2"))
            dismiss()
        }
        binding.color3.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "3"))
            dismiss()
        }
        binding.color4.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "4"))
            dismiss()
        }
        binding.color5.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "5"))
            dismiss()
        }
        binding.color6.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "6"))
            dismiss()
        }
        binding.color7.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "7"))
            dismiss()
        }
        binding.color8.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "8"))
            dismiss()
        }
        binding.color9.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "9"))
            dismiss()
        }
        binding.color10.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "10"))
            dismiss()
        }
        binding.color11.setOnClickListener {
            setFragmentResult("color", bundleOf("colorID" to "11"))
            dismiss()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {

      //TODO is this needed???
        super.onDismiss(dialog)

        val activity = requireActivity()
        if(activity is DialogInterface.OnDismissListener){
            activity.onDismiss(dialog)
        }

    }

}