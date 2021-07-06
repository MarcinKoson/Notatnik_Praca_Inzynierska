package com.thesis.note.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.entity.Tag
import com.thesis.note.database.entity.TagOfNote
import kotlinx.android.synthetic.main.recycler_view_tag_fragment_list.view.*
import kotlinx.android.synthetic.main.recycler_view_tag_list.view.*
//TODO

class AddTagFragmentAdapter (private val tagList:List<Tag>, onNoteListener: OnNoteListener) :
    RecyclerView.Adapter<AddTagFragmentAdapter.MyViewHolder>() {

    val mOnNoteListener = onNoteListener;

    class MyViewHolder(val textView: ConstraintLayout, val listener: OnNoteListener) : RecyclerView.ViewHolder(textView), View.OnClickListener{
        init{
            textView.setOnClickListener(this)
        }
        val onNoteListener = listener;

        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {

        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_tag_fragment_list, parent, false) as ConstraintLayout

        return MyViewHolder(textView,mOnNoteListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.textView.tagNameButton.text = tagList[position].Name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = tagList.size


    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}