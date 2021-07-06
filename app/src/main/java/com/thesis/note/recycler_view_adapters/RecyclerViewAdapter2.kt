package com.thesis.note.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
//TODO
class RecyclerViewAdapter2 (private val myDataset: Array<String>, onNoteListener: OnNoteListener) :
    RecyclerView.Adapter<RecyclerViewAdapter2.MyViewHolder>() {

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
                                    viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(textView,mOnNoteListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = myDataset[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size


    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}