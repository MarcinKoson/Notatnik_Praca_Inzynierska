package com.thesis.note.recycler_view_adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thesis.note.R
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.RecyclerViewNoteViewerImageBinding
import com.thesis.note.databinding.RecyclerViewNoteViewerTextBinding

/**
 * [RecyclerView] adapter for showing [Data] of [Note]
 */
class NoteViewerAdapter (private var dataList:List<Data>, private var onDataClickListener: OnDataClickListener)
    :RecyclerView.Adapter<NoteViewerAdapter.DataHolder>() {

    /**  */
    interface  OnDataClickListener {
        fun onDataClick(position:Int)
    }

    /**  */
    class DataHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnDataClickListener
        ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{

        init{
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onDataClick(adapterPosition)
        }
    }

    /**  */
    override fun getItemViewType(position: Int): Int {
        return dataList[position].Type.id
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): DataHolder {
        return when(viewType){
            NoteType.Text.id -> {
                DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_text, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            NoteType.Image.id -> {
                 DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_image, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            NoteType.Recording.id -> {
                //TODO layout for sound notes
                 DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_text, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            else -> {
                //TODO error handling
                 error("ERROR: NoteViewerAdapter - viewType not found")
            }
        }
    }

    /**  */
    override fun onBindViewHolder(holder: DataHolder, position: Int) {
       when(holder.itemViewType){
           NoteType.Text.id -> setTextData(holder, position)
           NoteType.Image.id -> setImageData(holder,position)
           NoteType.Recording.id -> setSoundData(holder, position)
       }
    }

    /**  */
    private fun setTextData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerTextBinding.bind(holder.objectLayout)
        binding.noteViewerTextContent.text = dataList[position].Content
        binding.noteViewerTextContent.setTextColor(
            holder.itemView.resources.getColor(
                NoteColorConverter.enumToColor(dataList[position].Color),null
            )
        )
        binding.noteViewerTextContent.textSize = dataList[position].Size?.toFloat()!!
        when(dataList[position].Info){
            "B" -> binding.noteViewerTextContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            "I" -> binding.noteViewerTextContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            "BI" -> binding.noteViewerTextContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        }
    }

    /**  */
    private fun setImageData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerImageBinding.bind(holder.objectLayout)
        Glide.with(holder.itemView)
            .load(dataList[position].Content)
            .fitCenter()
            .placeholder(R.drawable.ic_loading)
            .into(binding.noteViewerImage)
    }

    /** TODO sound data */
    private fun setSoundData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerTextBinding.bind(holder.objectLayout)
        binding.noteViewerTextContent.text = "NAGRANIE"
    }

    /**  */
    override fun getItemCount() = dataList.size

}
