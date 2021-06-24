package com.thesis.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.entity.Tag
import com.thesis.note.database.entity.TagOfNote
import kotlinx.android.synthetic.main.recycler_view_tag_list.view.*

class TagListAdapter (private val myDataset: List<TagOfNote>, private val tagList:List<Tag>, onNoteListener: OnNoteListener) :
    RecyclerView.Adapter<TagListAdapter.MyViewHolder>() {

    val mOnNoteListener = onNoteListener;

    class MyViewHolder(val textView: TextView, val listener: OnNoteListener) : RecyclerView.ViewHolder(textView), View.OnClickListener{
        init{
            textView.setOnClickListener(this)
        }
        val onNoteListener = listener;

        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TagListAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_tag_list, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(textView,mOnNoteListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.tagName.text = tagList.firstOrNull { x -> x.IdTag == myDataset[position].TagID }?.Name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size


    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}