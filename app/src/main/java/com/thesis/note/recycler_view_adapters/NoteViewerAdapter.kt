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
import com.thesis.note.databinding.RecyclerViewNoteViewerImageBinding
import com.thesis.note.databinding.RecyclerViewNoteViewerTextBinding

class NoteViewerAdapter (private var dataList:List<Data>, private var onDataClickListener: OnDataClickListener)
    :RecyclerView.Adapter<NoteViewerAdapter.DataHolder>() {

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
        return dataList[position].Type.id
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): DataHolder {
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

    override fun onBindViewHolder(holder: DataHolder, position: Int) {
       when(holder.itemViewType){
           NoteType.Text.id -> {
               val binding = RecyclerViewNoteViewerTextBinding.bind(holder.objectLayout)
               binding.noteViewerContent.text = dataList[position].Content
           }
           NoteType.Photo.id ->{
               val binding = RecyclerViewNoteViewerImageBinding.bind(holder.objectLayout)
               val imageUri = Uri.parse(dataList[position].Content)
               binding.noteViewerImage!!.setImageURI(imageUri)
           }
       }
    }

    override fun getItemCount() = dataList.size
}
