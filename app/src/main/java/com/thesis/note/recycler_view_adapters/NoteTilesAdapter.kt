package com.thesis.note.recycler_view_adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.RecyclerViewNoteListPhotoBinding
import com.thesis.note.databinding.RecyclerViewNoteListTextBinding
import com.thesis.note.databinding.RecyclerViewNoteTileTextBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  [RecyclerView] adapter for showing [Note]s in form of tiles
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
        //TODO test when note don't have main data
        val data = dataList.firstOrNull { v -> v.IdData == noteList[position].MainData }
        return data?.Type?.id ?: 0
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
            NoteType.Photo.id -> {
                //TODO layout for photos
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_list_photo,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
            NoteType.Sound.id -> {
                //TODO layout for sound notes
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_list_text,
                        parent,
                        false
                    ) as ConstraintLayout, onNoteClickListener
                )
            }
            else -> {
                //TODO error handling
                NoteTilesViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.recycler_view_note_list_text,
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
            NoteType.Photo.id -> setImageTile(holder, position)
            NoteType.Sound.id -> setSoundTile(holder, position)
        }
    }
    /**  */
    override fun getItemCount() = noteList.size

    /** */
    private fun setTextTile(holder: NoteTilesViewHolder, position: Int) {
        val mainData = dataList.firstOrNull { it.IdData == noteList[position].MainData }
        val binding = RecyclerViewNoteTileTextBinding.bind(holder.objectLayout)
        //name, favorite
        binding.noteName.text = noteList[position].Name
        binding.favoriteCheckBox.isChecked = noteList[position].Favorite
        //checking group
        if (noteList[position].GroupID != null)
            GlobalScope.launch {
                val groupName = AppDatabase(holder.objectLayout.context).groupDao()
                    .getId(noteList[position].GroupID!!).Name
                binding.groupName.text = groupName
            }
        //set listener for favorite button
        //TODO favorite check box colors
        binding.favoriteCheckBox.setOnClickListener {
            noteList[position].Favorite = binding.favoriteCheckBox.isChecked
            GlobalScope.launch {
                AppDatabase(holder.objectLayout.context).noteDao().update(noteList[position])
            }
        }
        //set content
        binding.noteContent.text = mainData?.Content
        //set graphic
        binding.noteContent.setTextColor(
            holder.itemView.resources.getColor(
                NoteColorConverter().enumToColor(
                    mainData?.Color
                ), null
            )
        )
        binding.noteContent.textSize = mainData?.Size?.toFloat()!!
        when (mainData.Info) {
            "B" -> binding.noteContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            "I" -> binding.noteContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            "BI" -> binding.noteContent.typeface =
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        }
        //set background
        binding.root.setBackgroundColor(
            holder.itemView.resources.getColor(NoteColorConverter().enumToColor(noteList[position].Color), null))

    }

    //TODO set image tile
    /** */
    private fun setImageTile(holder: NoteTilesViewHolder, position: Int) {
        val mainData = dataList.firstOrNull { it.IdData == noteList[position].MainData }
        val binding = RecyclerViewNoteListPhotoBinding.bind(holder.objectLayout)
        //name, favorite, note type
        binding.noteName.text = noteList[position].Name
        binding.favoriteCheckBox.isChecked = noteList[position].Favorite
        //checking group
        if (noteList[position].GroupID != null)
            GlobalScope.launch {
                val groupName = AppDatabase(holder.objectLayout.context).groupDao()
                    .getId(noteList[position].GroupID!!).Name
                binding.groupName.text = groupName
            }
        //set listener for favorite button
        binding.favoriteCheckBox.setOnClickListener(
            fun(_: View) {
                noteList[position].Favorite = binding.favoriteCheckBox.isChecked
                GlobalScope.launch {
                    AppDatabase(holder.objectLayout.context).noteDao()
                        .update(noteList[position])
                }
            }
        )
        //set content
        Glide.with(holder.itemView)
            .load(mainData?.Content)
            .fitCenter()
            .placeholder(R.drawable.ic_loading_24)
            .into(binding.noteContentImage)
    }

    //TODO set sound tile
    /** */
    private fun setSoundTile(holder: NoteTilesViewHolder, position: Int) {
        //get main data
        //val mainData = dataList.firstOrNull { it.IdData == noteList[position].MainData }
        val binding = RecyclerViewNoteListTextBinding.bind(holder.objectLayout)
        //name, favorite, note type
        binding.noteName.text = noteList[position].Name
        binding.favoriteCheckBox.isChecked = noteList[position].Favorite
        //checking group
        if (noteList[position].GroupID != null)
            GlobalScope.launch {
                val groupName = AppDatabase(holder.objectLayout.context).groupDao()
                    .getId(noteList[position].GroupID!!).Name
                binding.groupName.text = groupName
            }
        //set listener for favorite button
        binding.favoriteCheckBox.setOnClickListener(
            fun(_: View) {
                noteList[position].Favorite = binding.favoriteCheckBox.isChecked
                GlobalScope.launch {
                    AppDatabase(holder.objectLayout.context).noteDao()
                        .update(noteList[position])
                }
            }
        )
        //set content
        binding.noteContent.text = "NAGRANIE"
    }


    }