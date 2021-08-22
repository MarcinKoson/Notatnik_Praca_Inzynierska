package com.thesis.note.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.thesis.note.R
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Group
import com.thesis.note.databinding.DialogFragmentSearchBinding
import java.text.SimpleDateFormat
import java.util.*

//TODO tag search
/**
 *  Fragment for searching notes.
 *  When the user entered a search it invokes callback [searchListener].
 */
class SearchFragment(
    private val searchListener: SearchInterface,
    private val groupsList: List<Group>,
    private val lastSearchValues: SearchValues?,
    private val dateMin: String? = SearchConst.DATE_MIN
    ) : DialogFragment(){

    /** Constants */
    object SearchConst{
        /** Default minimum date */
        const val DATE_MIN = "01.08.2021"
    }

    /** Class to store information about search. [dateMax] and [dateMin] are String value with pattern "dd.MM.yyyy" */
    class SearchValues{
        var name: String? = null
        var content: String? = null
        var noteType: NoteType? = null
        /** Date pattern "dd.MM.yyyy" */
        var dateMin: String? = SearchConst.DATE_MIN
        /** Date pattern "dd.MM.yyyy" */
        var dateMax: String? = SimpleDateFormat("dd.MM.yyyy",Locale.US).format(Date())
        var favorite:Boolean = false
        var group:Int? = null
        var tags: List<Int>? = null
    }

    /** Interface definition for a callback to be invoked when when the user entered a search. */
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
                getString(R.string.fragment_search_without_type_of_data),
                getString(R.string.fragment_search_type_text),
                getString(R.string.fragment_search_type_image),
                getString(R.string.fragment_search_type_recording)
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
            insert(getString(R.string.fragment_search_without_group), 0)
        }

        //Load last search
        if(lastSearchValues != null){
            loadLastSearch()
        }
        else{
            binding.dateMinButton.text = dateMin
            binding.dateMaxButton.text = SimpleDateFormat("dd.MM.yyyy",Locale.US).format(Date())
        }

        //Min date button listener
        binding.dateMinButton.setOnClickListener {
            val date = Calendar.getInstance().apply{
                time = SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(binding.dateMinButton.text.toString()) ?: Date()
            }
            DatePickerDialog(
                this.requireContext(),
                { _,yyyy,MM,dd -> binding.dateMinButton.text= getString(R.string.fragment_search_date,dd,MM+1,yyyy) }, //it counts months form 0
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        //Max date button listener
        binding.dateMaxButton.setOnClickListener {
            val date = Calendar.getInstance().apply{
                time = SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(binding.dateMaxButton.text.toString()) ?: Date()
            }
            DatePickerDialog(
                this.requireContext(),
                { _,yyyy,MM,dd -> binding.dateMaxButton.text= getString(R.string.fragment_search_date,dd,MM+1,yyyy) }, //it counts months form 0
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)
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
                        2 -> NoteType.Image
                        3 -> NoteType.Recording
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

    /** Function for loading last search values into view */
    private fun loadLastSearch(){
        binding.editTextName.setText(lastSearchValues?.name)
        binding.editTextContent.setText(lastSearchValues?.content)
        binding.favoriteSearchCheckBox.isChecked = lastSearchValues?.favorite ?: false
        binding.typeOfNoteSpinner.setSelection(
            when(lastSearchValues?.noteType){
                NoteType.Text -> 1
                NoteType.Image -> 2
                NoteType.Recording -> 3
                else -> 0
            }
        )
        binding.dateMinButton.text = lastSearchValues?.dateMin
        binding.dateMaxButton.text = lastSearchValues?.dateMax
        if(lastSearchValues?.group != null)
            binding.groupSpinner.setSelection(1+groupsList.indexOf(groupsList.first { it.IdGroup == lastSearchValues.group }))
    }
}
