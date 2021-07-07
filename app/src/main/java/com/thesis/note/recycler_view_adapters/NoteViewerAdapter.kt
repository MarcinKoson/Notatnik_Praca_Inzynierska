package com.thesis.note.recycler_view_adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import kotlinx.android.synthetic.main.recycler_view_note_viewer_image.view.*
import kotlinx.android.synthetic.main.recycler_view_note_viewer_text.view.*

class NoteViewerAdapter (private var dataSet:List<Data>, private var onDataClickListener: OnDataClickListener)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface  OnDataClickListener {
        fun onDataClick(position:Int)
    }

    class DataHolder(val objectLayout: ConstraintLayout, val listener: OnDataClickListener)
        :RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            listener.onDataClick(adapterPosition);
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataSet[position].Type.id
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): RecyclerView.ViewHolder {
        when(viewType){
            NoteType.Text.id -> {
                return DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_text, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            NoteType.Photo.id -> {
                return DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_image, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
        }
        return error("ERROR: NoteViewerAdapter - viewType not found")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       when(holder.itemViewType){
           NoteType.Text.id -> {
               val viewHolder0: DataHolder = holder as DataHolder
               viewHolder0.objectLayout.note_viewer_content.text = dataSet[position].Content
           }
           NoteType.Photo.id ->{
               val viewHolder0: DataHolder = holder as DataHolder
               val imageUri = Uri.parse(dataSet[position].Content)
               viewHolder0.objectLayout.note_viewer_image!!.setImageURI(imageUri)
           }
       }
    }

    override fun getItemCount() = dataSet.size
}
