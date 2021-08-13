package com.thesis.note.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.thesis.note.databinding.DialogFragmentSortNotesBinding

/**
 *  Fragment for picking type of sorting.
 *  After selecting it sets fragment result with resultKey 'sort'
 *  and bundle with [SortNotesType.id] as Int 'sortType' and
 *  if is ascending as Boolean 'sortAsc'
 *
 */
class SortNotesFragment(private val sortType:SortNotesType, private val sortAsc:Boolean):DialogFragment(){

    /** View binding */
    private lateinit var binding: DialogFragmentSortNotesBinding

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentSortNotesBinding.inflate(requireActivity().layoutInflater)

        if(sortAsc){
            binding.sortAsc.isChecked = true
        } else{
            binding.sortDesc.isChecked = true
        }
        when(sortType){
            SortNotesType.Alphabetically -> binding.sortAlphabetically.isChecked = true
            SortNotesType.Date -> binding.sortDate.isChecked = true
            SortNotesType.Group -> binding.sortGroup.isChecked = true
        }
        binding.dialogSortButton.setOnClickListener {
            val bundle = Bundle()
            if(binding.sortAsc.isChecked){
                bundle.putBoolean("sortAsc", true)
            } else {
                bundle.putBoolean("sortAsc", false)
            }
            when(binding.radioGroupSort.checkedRadioButtonId){
                binding.sortAlphabetically.id -> bundle.putInt("sortType",SortNotesType.Alphabetically.id)
                binding.sortDate.id -> bundle.putInt("sortType",SortNotesType.Date.id)
                binding.sortGroup.id -> bundle.putInt("sortType",SortNotesType.Group.id)
            }
            setFragmentResult("sort", bundle)
            dismiss()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}