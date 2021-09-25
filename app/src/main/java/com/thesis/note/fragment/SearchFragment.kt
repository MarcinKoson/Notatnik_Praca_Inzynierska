package com.thesis.note.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.thesis.note.R
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Group
import com.thesis.note.database.entity.Tag
import com.thesis.note.databinding.DialogFragmentSearchBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 *  Fragment for searching notes.
 *  When the user entered a search it invokes callback [searchListener].
 */
class SearchFragment(
    private val searchListener: SearchInterface,
    private val groupsList: List<Group>,
    private val tagsList: List<Tag>,
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
        var tag:Int? = null

        override fun toString():String{
            return "${name.let { it ?: "" }}/n" +
                    "${content.let { it ?: "" }}/n" +
                    "${noteType?.name ?: ""}/n" +
                    "${dateMin.let { it ?: "" }}/n" +
                    "${dateMax.let { it ?: "" }}/n" +
                    "$favorite/n" +
                    "${group.let { it ?: "" }}/n" +
                    "${tag.let { it ?: "" }}/n"
        }

        fun fromString(string: String){
            val splitted = string.split("/n")
            name = if(splitted[0]=="") null else splitted[0]
            content = if(splitted[1]=="") null else splitted[1]
            noteType = if(splitted[2]=="") null else NoteType.valueOf(splitted[2])
            dateMin = if(splitted[3]=="") null else splitted[3]
            dateMax = if(splitted[4]=="") null else splitted[4]
            favorite = splitted[5].toBoolean()
            group = splitted[6].toIntOrNull()
            tag = splitted[7].toIntOrNull()
        }
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
        loadSettings()
        //Type of note spinner
        binding.typeOfNoteSpinner.adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            listOf(
                getString(R.string.fragment_search_without_type_of_data),
                getString(R.string.fragment_search_type_text),
                getString(R.string.fragment_search_type_list),
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

        //Tags spinner
        binding.tagSpinner.adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_spinner_item,
            tagsList.map { x -> x.Name }
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
                tag = binding.tagSpinner.selectedItemId.toInt().let {
                    if(it == 0) null else tagsList[it-1].IdTag
                }
                noteType = binding.typeOfNoteSpinner.selectedItemId.toInt().let {
                    when(it){
                        0 -> null
                        1 -> NoteType.Text
                        2 -> NoteType.List
                        3 -> NoteType.Image
                        4 -> NoteType.Recording
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
                NoteType.List -> 2
                NoteType.Image -> 3
                NoteType.Recording -> 4
                else -> 0
            }
        )
        binding.dateMinButton.text = lastSearchValues?.dateMin
        binding.dateMaxButton.text = lastSearchValues?.dateMax
        if(lastSearchValues?.group != null)
            binding.groupSpinner.setSelection(1+groupsList.indexOf(groupsList.first { it.IdGroup == lastSearchValues.group }))
        if(lastSearchValues?.tag != null)
            binding.tagSpinner.setSelection(1+tagsList.indexOf(tagsList.first { it.IdTag == lastSearchValues.tag }))
    }

    /** Load settings related to this fragment */
    private fun loadSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
        if(!sharedPreferences.getBoolean("search_name", true)){
            binding.nameLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.editTextName.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
        if(!sharedPreferences.getBoolean("search_content", true)){
            binding.contentLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.editTextContent.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
        if(!sharedPreferences.getBoolean("search_favorite", true)){
            binding.favoriteSearchCheckBox.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
        if(!sharedPreferences.getBoolean("search_type", true)){
            binding.typeOfNoteLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.typeOfNoteSpinner.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
        if(!sharedPreferences.getBoolean("search_date", true)){
            binding.dateLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.dateMinButton.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.dateHelpLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.dateMaxButton.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
        if(!sharedPreferences.getBoolean("search_group", true)){
            binding.groupLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.groupSpinner.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
        if(!sharedPreferences.getBoolean("search_tag", true)){
            binding.tagsLabel.apply{
                isEnabled = false
                visibility = View.GONE
            }
            binding.tagSpinner.apply{
                isEnabled = false
                visibility = View.GONE
            }
        }
    }
}
