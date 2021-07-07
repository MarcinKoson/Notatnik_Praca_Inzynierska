package com.thesis.note.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.entity.Tag
import kotlinx.android.synthetic.main.recycler_view_add_tag_fragment.view.*


class AddTagFragmentAdapter (private val tagList:List<Tag>, private val onTagClickListener: OnTagClickListener)
    :RecyclerView.Adapter<AddTagFragmentAdapter.TagHolder>() {

    interface  OnTagClickListener {
        fun onTagClick(position:Int)
    }

    class TagHolder(val objectLayout: ConstraintLayout, val listener: OnTagClickListener)
        :RecyclerView.ViewHolder(objectLayout), View.OnClickListener {
        init {
            objectLayout.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            listener.onTagClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): TagHolder {
        return TagHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_add_tag_fragment, parent, false) as ConstraintLayout,
            onTagClickListener)
    }

    override fun onBindViewHolder(holder: TagHolder, position: Int) {
        holder.objectLayout.tagNameButton.text = tagList[position].Name
    }

    override fun getItemCount() = tagList.size
}
