package com.thesis.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.R
import kotlinx.android.synthetic.main.recycler_view_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NoteViewerAdapter (private var dataSet:List<Data>, onNoteListener: OnNoteListener) :
    RecyclerView.Adapter<NoteViewerAdapter.MyViewHolder>() {

    val mOnNoteListener = onNoteListener;

    fun changeDataset(newData: List<Data>){
        dataSet = newData
        notifyDataSetChanged()
    }

    class MyViewHolder(val objectLayout: ConstraintLayout, val listener: OnNoteListener) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        val onNoteListener = listener;
        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): NoteViewerAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_viewer_recycler_view_layout, parent, false) as ConstraintLayout
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(textView,mOnNoteListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.objectLayout.noteContent.text = dataSet[position].Content

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}