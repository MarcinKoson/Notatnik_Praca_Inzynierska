package com.thesis.note

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Tag
import com.thesis.note.database.entity.TagOfNote
import com.thesis.note.recycler_view_adapters.AddTagFragmentAdapter
import com.thesis.note.recycler_view_adapters.NoteViewerAdapter
import com.thesis.note.recycler_view_adapters.RecyclerViewAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTagsDialogFragment : DialogFragment()
    {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var tagsList: List<Tag>
    private lateinit var tagsOfNoteList: List<TagOfNote>
    private lateinit var filteredTagsList: List<Tag>

    private var noteID : Int = -1

    lateinit var db: AppDatabase

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_note,null)
        //load tags
        db = AppDatabase(view.context)
        noteID = requireArguments().getInt("noteID")
        GlobalScope.launch {

            tagsList = db.tagDao().getAll()
            tagsOfNoteList = db.tagOfNoteDAO().getAllNoteTags(noteID)
            filteredTagsList = tagsList.filter { tag -> !(tagsOfNoteList.any { tagOfNote -> tagOfNote.TagID == tag.IdTag }) }

            viewManager = FlexboxLayoutManager(view.context)
            viewAdapter = AddTagFragmentAdapter(filteredTagsList,
                object:AddTagFragmentAdapter.OnNoteListener{
                    override fun onNoteClick(position: Int) {
                        GlobalScope.launch {
                            db.tagOfNoteDAO().insertAll(TagOfNote(0,filteredTagsList[position].IdTag,noteID))
                        }
                        dismiss()
                    }
                } )
            recyclerView = view.findViewById<RecyclerView>(R.id.listOfAllTags).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setView(view)
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
