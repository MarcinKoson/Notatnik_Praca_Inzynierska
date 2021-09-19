package com.thesis.note.recycler_view_adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thesis.note.Constants
import com.thesis.note.R
import com.thesis.note.database.*
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  [RecyclerView] adapter for showing [Note]s in form of tiles
 *
 */
class NoteTilesAdapter (
        private var noteList: List<Note>,
        private var dataList:List<Data>,
        private val onNoteClickListener: OnNoteClickListener
    ) : RecyclerView.Adapter<NoteTilesAdapter.NoteTilesViewHolder>() {

    /**  */
    interface OnNoteClickListener {
        fun onNoteClick(position: Int)
    }

    /**  */
    class NoteTilesViewHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnNoteClickListener
    ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener {

        init {
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onNoteClick(adapterPosition)
        }
    }

    /**  */
    override fun getItemViewType(position: Int): Int {
        val data = dataList.firstOrNull { v -> v.IdData == noteList[position].MainData }
        return data?.Type?.id ?: -1
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteTilesViewHolder {
        return when (viewType) {
            NoteType.Text.id -> {
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_tile_text,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
            NoteType.List.id -> {
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_tile_list,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
            NoteType.Image.id -> {
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_tile_image,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
            NoteType.Recording.id -> {
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_tile_recording,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
            else -> {
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_tile_text,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
        }
    }

    /**  */
    override fun onBindViewHolder(holder: NoteTilesViewHolder, position: Int) {
        when (holder.itemViewType) {
            NoteType.Text.id -> setTextTile(holder, position)
            NoteType.List.id -> setListTile(holder, position)
            NoteType.Image.id -> setImageTile(holder, position)
            NoteType.Recording.id -> setRecordingTile(holder, position)
            else -> {
                val binding = RecyclerViewNoteTileTextBinding.bind(holder.objectLayout)
                setNoteInfo(holder,position,binding.root,binding.noteName,binding.favoriteCheckBox,binding.groupName)
            }
        }
    }

    /**  */
    override fun getItemCount() = noteList.size

    /**  */
    private fun setNoteInfo(
        holder: NoteTilesViewHolder,
        position: Int,
        root:ConstraintLayout,
        noteName:TextView,
        favoriteCheckBox:CheckBox,
        groupName:TextView
    ){
        //name
        noteName.text = noteList[position].Name
        noteName.textSize = Constants.TEXT_SIZE_BIG
        noteName.setTextColor(ContextCompat.getColor(holder.objectLayout.context, R.color.black))
        //favorite
        favoriteCheckBox.isChecked = noteList[position].Favorite
        //set listener for favorite button
        favoriteCheckBox.setOnClickListener {
            noteList[position].Favorite = favoriteCheckBox.isChecked
            GlobalScope.launch {
                AppDatabase(holder.objectLayout.context).noteDao().update(noteList[position])
            }
        }
        //set background
        root.setBackgroundColor(
            holder.itemView.resources.getColor(NoteColorConverter.enumToColor(noteList[position].Color), null))
        //checking group
        if (noteList[position].GroupID != null)
            GlobalScope.launch {
                groupName.text = AppDatabase(holder.objectLayout.context).groupDao().getId(noteList[position].GroupID!!).Name
            }
    }
    /**  */
    private fun setTextTile(holder: NoteTilesViewHolder, position: Int) {
        val binding = RecyclerViewNoteTileTextBinding.bind(holder.objectLayout)
        setNoteInfo(holder,position,binding.root,binding.noteName,binding.favoriteCheckBox,binding.groupName)
        val mainData = dataList.firstOrNull { it.IdData == noteList[position].MainData }
        //set content
        binding.noteContent.text = mainData?.Content
        //set graphic
        binding.noteContent.setTextColor(
            holder.itemView.resources.getColor(
                NoteColorConverter.enumToColor(
                    mainData?.Color
                ), null
            )
        )
        binding.noteContent.textSize = mainData?.Size?.toFloat()!!
        when (mainData.Info) {
            "B" -> binding.noteContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            "I" -> binding.noteContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            "BI" -> binding.noteContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        }
        //note name size
        binding.noteName.textSize = mainData.Size?.toFloat()!!+5
    }

    /**  */
    private fun setListTile(holder: NoteTilesViewHolder, position: Int){
        val binding = RecyclerViewNoteTileListBinding.bind(holder.objectLayout)
        setNoteInfo(holder,position,binding.root,binding.noteName,binding.favoriteCheckBox,binding.groupName)
        val mainData = ListData().apply { dataList.firstOrNull { it.IdData == noteList[position].MainData }?.let { loadData(it) } }
        var noteContent = ""
        mainData.itemsList.forEach {
            if(!it.checked){
                noteContent += "•" + it.text + "\r\n"
            }
        }
        binding.noteContent.text = noteContent
    }

    /**  */
    private fun setImageTile(holder: NoteTilesViewHolder, position: Int) {
        val binding = RecyclerViewNoteTileImageBinding.bind(holder.objectLayout)
        setNoteInfo(holder,position,binding.root,binding.noteName,binding.favoriteCheckBox,binding.groupName)
        val mainData = dataList.firstOrNull { it.IdData == noteList[position].MainData }
        //set content
        Glide.with(holder.itemView)
            .load(mainData?.Content)
            .fitCenter()
            .placeholder(R.drawable.ic_loading)
            .into(binding.noteContentImage)
    }

    /**  */
    private fun setRecordingTile(holder: NoteTilesViewHolder, position: Int) {
        val binding = RecyclerViewNoteTileRecordingBinding.bind(holder.objectLayout)
        setNoteInfo(holder,position,binding.root,binding.noteName,binding.favoriteCheckBox,binding.groupName)
        Glide.with(holder.itemView)
            .load(getDrawable(holder.objectLayout.context,R.drawable.ic_baseline_music_note))
            .fitCenter()
            .into(binding.noteRecordingImage)
    }
}
