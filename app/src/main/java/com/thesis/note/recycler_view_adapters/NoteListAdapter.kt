package com.thesis.note.recycler_view_adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.RecyclerViewNoteListPhotoBinding
import com.thesis.note.databinding.RecyclerViewNoteListTextBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min


class NoteListAdapter (private var noteList: List<Note>, private var dataList:List<Data>, private val onNoteClickListener: OnNoteClickListener)
    :RecyclerView.Adapter<NoteListAdapter.NoteListViewHolder>() {

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
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_list_text, parent, false) as ConstraintLayout
                    ,onNoteClickListener
                   )
            }
            NoteType.Photo.id -> {
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_list_photo, parent, false) as ConstraintLayout
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

        //note type string
        val noteTypeStr: String = when(mainData?.Type){
            NoteType.Text -> holder.objectLayout.context.getString(R.string.note_type_text)
            NoteType.List -> holder.objectLayout.context.getString(R.string.note_type_list)
            NoteType.Video -> holder.objectLayout.context.getString(R.string.note_type_video)
            NoteType.Sound -> holder.objectLayout.context.getString(R.string.note_type_sound)
            NoteType.Photo -> holder.objectLayout.context.getString(R.string.note_type_photo)
            else -> holder.objectLayout.context.getString(R.string.note_type_other)
        }

        when(holder.itemViewType){
            NoteType.Text.id -> {
                val binding = RecyclerViewNoteListTextBinding.bind(holder.objectLayout)
                //name, favorite, note type
                binding.noteName.text = noteList[position].Name
                binding.favoriteCheckBox.isChecked = noteList[position].Favorite
                binding.noteType.text = noteTypeStr
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
            }
            NoteType.Photo.id -> {
                val binding = RecyclerViewNoteListPhotoBinding.bind(holder.objectLayout)
                //name, favorite, note type
                binding.noteName.text = noteList[position].Name
                binding.favoriteCheckBox.isChecked = noteList[position].Favorite
                binding.noteType.text = noteTypeStr
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
                ///binding.noteContentImage.setImageURI(Uri.parse(mainData?.Content))
                setImage(binding, mainData?.Content )
            }
        }
    }

    override fun getItemCount() = noteList.size

    private fun setImage(binding : RecyclerViewNoteListPhotoBinding, path:String?) {
        //TODO image scaling
        val opts = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            inJustDecodeBounds = false
            inSampleSize = max(1, min(outWidth / 150, outHeight / 150))
        }
        binding.noteContentImage.setImageBitmap(BitmapFactory.decodeFile(path, opts))
    }
}
