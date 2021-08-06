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
import com.thesis.note.databinding.RecyclerViewNoteTextBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NoteTilesListAdapter (private var noteList: List<Note>, private var dataList:List<Data>, private val onNoteClickListener: OnNoteClickListener)
    :RecyclerView.Adapter<NoteTilesListAdapter.NoteListViewHolder>() {

    interface  OnNoteClickListener {
        fun onNoteClick(position:Int)
    }

    class NoteListViewHolder(val objectLayout: ConstraintLayout, val listener: OnNoteClickListener)
        : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            listener.onNoteClick(adapterPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val data = dataList.firstOrNull{ v -> v.IdData == noteList[position].MainData }
        //TODO test when note don't have main data
        return data?.Type?.id ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): NoteListViewHolder {
        //TODO create new layouts
        when(viewType){
            NoteType.Text.id -> {
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_text, parent, false) as ConstraintLayout
                    ,onNoteClickListener
                   )
            }
            NoteType.Photo.id -> {
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_list_photo, parent, false) as ConstraintLayout
                    ,onNoteClickListener
                    )
            }
            NoteType.Sound.id -> {
                //TODO layout for sound notes
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_list_text, parent, false) as ConstraintLayout
                    ,onNoteClickListener
                )
            }
            else ->{
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_list_text, parent, false) as ConstraintLayout
                    ,onNoteClickListener
                    )
            }
        }
    }

    override fun onBindViewHolder(holder: NoteListViewHolder, position: Int) {
        //get main data
        val mainData = dataList.firstOrNull{it.IdData == noteList[position].MainData}

        when(holder.itemViewType){
            NoteType.Text.id -> {
                val binding = RecyclerViewNoteTextBinding.bind(holder.objectLayout)
                //name, favorite, note type
                binding.noteName.text = noteList[position].Name
                binding.favoriteCheckBox.isChecked = noteList[position].Favorite
                //checking group
                if(noteList[position].GroupID !=null)
                    GlobalScope.launch {
                        val groupName = AppDatabase(holder.objectLayout.context).groupDao().getId(noteList[position].GroupID!!).Name
                        binding.tagName.text = groupName
                    }
                //set listener for favorite button
                binding.favoriteCheckBox.setOnClickListener (
                    fun (_:View){
                        noteList[position].Favorite = binding.favoriteCheckBox.isChecked
                        GlobalScope.launch{
                            AppDatabase(holder.objectLayout.context).noteDao().update(noteList[position])
                        }
                    }
                )
                //set content
                binding.noteListContent.text = mainData?.Content
                //set graphic
                binding.noteListContent.setTextColor(
                    holder.itemView.resources.getColor(
                        NoteColorConverter().enumToColor(mainData?.Color),null))
                binding.noteListContent.textSize = mainData?.Size?.toFloat()!!
                when(mainData?.Info){
                    "B" -> binding.noteListContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    "I" -> binding.noteListContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    "BI" -> binding.noteListContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                }
                //set background
                binding.root.setBackgroundColor(holder.itemView.resources.getColor(NoteColorConverter().enumToColor(noteList[position].Color),null))
            }
            NoteType.Photo.id -> {
                val binding = RecyclerViewNoteListPhotoBinding.bind(holder.objectLayout)
                //name, favorite, note type
                binding.noteName.text = noteList[position].Name
                binding.favoriteCheckBox.isChecked = noteList[position].Favorite
                //checking group
                if(noteList[position].GroupID !=null)
                    GlobalScope.launch {
                        val groupName = AppDatabase(holder.objectLayout.context).groupDao().getId(noteList[position].GroupID!!).Name
                        binding.tagName.text = groupName
                    }
                //set listener for favorite button
                binding.favoriteCheckBox.setOnClickListener (
                    fun (_:View){
                        noteList[position].Favorite = binding.favoriteCheckBox.isChecked
                        GlobalScope.launch{
                            AppDatabase(holder.objectLayout.context).noteDao().update(noteList[position])
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
            NoteType.Sound.id -> {
                val binding = RecyclerViewNoteListTextBinding.bind(holder.objectLayout)
                //name, favorite, note type
                binding.noteName.text = noteList[position].Name
                binding.favoriteCheckBox.isChecked = noteList[position].Favorite
                //checking group
                if(noteList[position].GroupID !=null)
                    GlobalScope.launch {
                        val groupName = AppDatabase(holder.objectLayout.context).groupDao().getId(noteList[position].GroupID!!).Name
                        binding.tagName.text = groupName
                    }
                //set listener for favorite button
                binding.favoriteCheckBox.setOnClickListener (
                    fun (_:View){
                        noteList[position].Favorite = binding.favoriteCheckBox.isChecked
                        GlobalScope.launch{
                            AppDatabase(holder.objectLayout.context).noteDao().update(noteList[position])
                        }
                    }
                )
                //set content
                binding.noteListContent.text = "NAGRANIE \n\n"
            }
        }
    }

    override fun getItemCount() = noteList.size
    }