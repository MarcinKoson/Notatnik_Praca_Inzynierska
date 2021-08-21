package com.thesis.note.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Group
import com.thesis.note.databinding.DialogFragmentSearchBinding

/**
 *  Fragment for searching notes
 * TODO tag search
 */
class SearchFragment(
    private val searchListener:SearchInterface,
    private val groupsList: List<Group>,
    private val lastSearchValues: SearchValues?
    ) : DialogFragment(){

    /** Class to store information about search */
    class SearchValues{
        var name: String? = null
        var content: String? = null
        var noteType: NoteType? = null
        //TODO dateMin and dateMax
        var dateMin: String? = "1-8-2021"
        var dateMax: String? = "1-10-2021"
        var favorite:Boolean = false
        var group:Int? = null
        var tags: List<Int>? = null
    }

    /** Interface with listener for sending [SearchValues] */
    interface SearchInterface {
        fun onSearchClick(searchValues:SearchValues)
    }

    /** View binding */
    private lateinit var binding: DialogFragmentSearchBinding

    /** Info about current search */
    private lateinit var searchValues: SearchValues

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentSearchBinding.inflate(requireActivity().layoutInflater)

        //Type of note spinner
        binding.typeOfNoteSpinner.adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            listOf(
                getString(com.thesis.note.R.string.fragment_search_without_type_of_data),
                getString(com.thesis.note.R.string.fragment_search_type_text),
                getString(com.thesis.note.R.string.fragment_search_type_image),
                getString(com.thesis.note.R.string.fragment_search_type_recording)
            )
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        //Group spinner
        binding.groupSpinner.adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            groupsList.map { x -> x.Name }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            insert(getString(com.thesis.note.R.string.fragment_search_without_group), 0)
        }

        //TODO set default date

        //Load last search
        loadLastSearch()

        //Min date button listener
        //TODO default and start min date
        binding.dateMinButton.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                { _,a,b,c -> binding.dateMinButton.text= "$c-$b-$a" },
                2021, 8, 1
            ).show()
        }

        //Max date button listener
        //TODO default and start max date
        binding.dateMaxButton.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                {_,a,b,c -> binding.dateMaxButton.text= "$c-$b-$a" },
                2021, 10, 1
            ).show()
        }

        //Search button
        binding.dialogSearchButton.setOnClickListener {
            searchValues = SearchValues().apply {
                name = binding.editTextName.text.toString().let{ if(it == "") null else it }
                content = binding.editTextContent.text.toString().let{ if(it == "") null else it }
                favorite = binding.favoriteSearchCheckBox.isChecked
                group = binding.groupSpinner.selectedItemId.toInt().let {
                    if(it == 0) null else groupsList[it-1].IdGroup
                }
                noteType = binding.typeOfNoteSpinner.selectedItemId.toInt().let {
                    when(it){
                        0 -> null
                        1 -> NoteType.Text
                        2 -> NoteType.Photo
                        3 -> NoteType.Sound
                        else -> null
                    }
                }
                dateMin = binding.dateMinButton.text.toString()
                dateMax = binding.dateMaxButton.text.toString()
            }
            searchListener.onSearchClick(searchValues)
            dismiss()
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun loadLastSearch(){
        binding.editTextName.setText(lastSearchValues?.name)
        binding.editTextContent.setText(lastSearchValues?.content)
        binding.favoriteSearchCheckBox.isChecked = lastSearchValues?.favorite ?: false
        binding.typeOfNoteSpinner.setSelection(
            when(lastSearchValues?.noteType){
                NoteType.Text -> 1
                NoteType.Photo -> 2
                NoteType.Sound -> 3
                else -> 0
            }
        )
        binding.dateMinButton.text = lastSearchValues?.dateMin
        binding.dateMaxButton.text = lastSearchValues?.dateMax
        if(lastSearchValues?.group != null)
            binding.groupSpinner.setSelection(
                1+groupsList.indexOf(groupsList.first { it.IdGroup == lastSearchValues.group }))

    }
}
