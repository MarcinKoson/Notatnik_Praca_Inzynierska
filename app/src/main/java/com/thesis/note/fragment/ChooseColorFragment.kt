package com.thesis.note.fragment

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.TagOfNote
import com.thesis.note.databinding.DialogChooseColorBinding
import com.thesis.note.recycler_view_adapters.AddTagFragmentAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChooseColorFragment:DialogFragment(){
    private lateinit var binding: DialogChooseColorBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_note,null)
        binding = DialogChooseColorBinding.inflate(requireActivity().layoutInflater)

        binding.color1.setOnClickListener {
            setFragmentResult("requestKey", bundleOf("bundleKey" to "1"))
            dismiss()
        }
        binding.color2.setOnClickListener {
            setFragmentResult("requestKey", bundleOf("bundleKey" to "2"))
            dismiss()
        }
        binding.color3.setOnClickListener {
            setFragmentResult("requestKey", bundleOf("bundleKey" to "3"))
            dismiss()
        }


        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val activity = requireActivity()
        if(activity is DialogInterface.OnDismissListener){
            activity.onDismiss(dialog)
        }

    }

}