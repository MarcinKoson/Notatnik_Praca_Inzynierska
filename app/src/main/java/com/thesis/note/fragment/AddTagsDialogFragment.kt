package com.thesis.note.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Tag
import com.thesis.note.database.entity.TagOfNote
import com.thesis.note.databinding.DialogFragmentAddTagsBinding
import com.thesis.note.recycler_view_adapters.AddTagFragmentAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  Fragment for adding tag into note.
 *  When created you must [setArguments] with int 'noteID'.
 *  It shows tags that is not already in note.
 *  If clicked on tag it add [TagOfNote] into database.
 *  After adding it sets fragment result with resultKey 'addedTag'
 *  and bundle with 'tagID'
 *
 */
class AddTagsDialogFragment : DialogFragment(){
    /** View binding */
    private lateinit var binding: DialogFragmentAddTagsBinding

    /** Database */
    lateinit var db: AppDatabase

    /**  */
    private var noteID : Int = -1
    /**  */
    private lateinit var filteredTagsList: List<Tag>

    /** On create dialog callback */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentAddTagsBinding.inflate(requireActivity().layoutInflater)
        db = AppDatabase(binding.root.context)
        //get note id
        noteID = requireArguments().getInt("noteID")
        GlobalScope.launch {
            //load data from db
            val tagsList = db.tagDao().getAll()
            val tagsOfNoteList = db.tagOfNoteDAO().getAllNoteTags(noteID)
            filteredTagsList = tagsList.filter { tag -> !(tagsOfNoteList.any { tagOfNote -> tagOfNote.TagID == tag.IdTag }) }
            //recycler view init
            val viewManager = FlexboxLayoutManager(binding.root.context)
            val viewAdapter = AddTagFragmentAdapter(
                filteredTagsList,
                //set listener - if tag is clicked add it to note
                object:AddTagFragmentAdapter.OnTagClickListener{
                    override fun onTagClick(position: Int) {
                        GlobalScope.launch {
                            db.tagOfNoteDAO().insertAll(TagOfNote(0,filteredTagsList[position].IdTag,noteID))
                            setFragmentResult("addedTag", bundleOf("tagID" to filteredTagsList[position].IdTag))
                        }
                        dismiss()
                    }
                }
            )
            binding.listOfAllTags.apply{
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
