package com.thesis.note.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.entity.Tag
import com.thesis.note.database.entity.TagOfNote
import com.thesis.note.databinding.RecyclerViewTagListBinding

class TagListAdapter (private val tagOfNoteList: List<TagOfNote>, private val tagList:List<Tag>, private val onTagClickListener: OnTagClickListener)
    :RecyclerView.Adapter<TagListAdapter.TagHolder>() {

    interface  OnTagClickListener {
        fun onNoteClick(position:Int)
    }

    class TagHolder(val objectLayout: ConstraintLayout, val listener: OnTagClickListener)
        :RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            listener.onNoteClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): TagHolder {
        return TagHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_tag_list, parent, false) as ConstraintLayout
            ,onTagClickListener)
    }

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        val binding = RecyclerViewTagListBinding.bind(holder.objectLayout)
        binding.tagName.text = tagList.firstOrNull { x -> x.IdTag == tagOfNoteList[position].TagID }?.Name
    }

    override fun getItemCount() = tagOfNoteList.size

}
