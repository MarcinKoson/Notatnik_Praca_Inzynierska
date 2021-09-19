package com.thesis.note.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.database.NoteColor
import com.thesis.note.databinding.DialogFragmentColorPickerBinding
import com.thesis.note.recycler_view_adapters.ColorListAdapter
import com.thesis.note.recycler_view_adapters.ColorListAdapter.OnColorClickListener

/**
 *  Fragment for picking color. It takes list of colors and shows them
 *  After selecting a color it sets fragment result with resultKey 'color'
 *  and bundle with [NoteColor.id] as Int 'colorID'
 *
 */
class ColorPickerFragment(private val colorList:List<NoteColor>):DialogFragment(){

    /** View binding */
    private lateinit var binding: DialogFragmentColorPickerBinding

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentColorPickerBinding.inflate(requireActivity().layoutInflater)
        val viewManager = FlexboxLayoutManager(binding.root.context)
        val viewAdapter = ColorListAdapter(colorList, object: OnColorClickListener{
            override fun onColorClick(position: Int) {
                setFragmentResult("color", bundleOf("colorID" to colorList[position].id))
                dismiss()
            }
        })
        binding.colorsRecyclerView.apply{
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
