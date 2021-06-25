package com.thesis.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.database.NoteType
import com.thesis.note.database.NoteTypeConverter
import com.thesis.note.database.entity.Data
import kotlinx.android.synthetic.main.recycler_view_layout.view.*

class NoteViewerAdapter (private var dataSet:List<Data>, onNoteListener: OnNoteListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val mOnNoteListener = onNoteListener;

    fun changeDataset(newData: List<Data>){
        dataSet = newData
        notifyDataSetChanged()
    }

    class TextViewHolder(val objectLayout: ConstraintLayout, val listener: OnNoteListener) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        val onNoteListener = listener;
        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }
    }
    class OtherViewHolder(val objectLayout: ConstraintLayout, val listener: OnNoteListener) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        val onNoteListener = listener;
        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataSet[position].Type.id
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            //NoteType.Text
            0 -> {
                val textView = LayoutInflater.from(parent.context).inflate(R.layout.note_viewer_recycler_view_layout, parent, false) as ConstraintLayout
                return TextViewHolder(textView,mOnNoteListener)
            }
            1 -> {

            }
        }
        return error("ERROR: NoteViewerAdapter - viewType not found")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       when(holder.itemViewType){
           0 -> {
               val viewHolder0:TextViewHolder = holder as TextViewHolder
               viewHolder0.objectLayout.noteContent.text = dataSet[position].Content
           }
           1 -> {

           }
       }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}