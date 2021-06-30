package com.thesis.note.recycler_view_adapters

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


import kotlinx.android.synthetic.main.recycler_view_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//TODO
class NoteListAdapter (private var noteList: List<Note>, private var dataList:List<Data>, private val onNoteClickListener: OnNoteClickListener) :
    RecyclerView.Adapter<NoteListAdapter.NoteListViewHolder>() {

    class NoteListViewHolder(val objectLayout: ConstraintLayout, val listener: OnNoteClickListener) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
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
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_layout, parent, false) as ConstraintLayout ,
                    onNoteClickListener)
            }
            NoteType.Photo.id -> {
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_layout, parent, false) as ConstraintLayout ,
                    onNoteClickListener)
            }
            else ->{
                return NoteListViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_layout, parent, false) as ConstraintLayout ,
                    onNoteClickListener)
            }
        }
    }

    override fun onBindViewHolder(holder: NoteListViewHolder, position: Int) {
        //get main data
        val mainData = dataList.firstOrNull{it.IdData == noteList[position].MainData}
        //name and favorite
        holder.objectLayout.noteName.text = noteList[position].Name
        holder.objectLayout.favoriteCheckBox.isChecked = noteList[position].Favorite
        //note type
        //TODO use string.xml
        val noteTypeStr: String = when(mainData?.Type){
            NoteType.Text -> "Tekstowa"
            NoteType.List -> "Lista"
            NoteType.Video -> "Wideo"
            NoteType.Sound -> "Dziękowa"
            NoteType.Photo -> "Obraz"
            else -> "Inna"
        }
        holder.objectLayout.noteType.text = noteTypeStr
        //checking group
        if(noteList[position].GroupID !=null)
            GlobalScope.launch {
                val groupName = AppDatabase(holder.objectLayout.context).groupDao().getId(noteList[position].GroupID!!).Name
                holder.objectLayout.tagName.text = groupName
            }
        //set listener for favorite button
        holder.objectLayout.favoriteCheckBox.setOnClickListener (
            fun (_:View){
                noteList[position].Favorite = holder.objectLayout.favoriteCheckBox.isChecked
                GlobalScope.launch{
                    AppDatabase(holder.objectLayout.context).noteDao().updateTodo(noteList[position])
                }
            }
        )
        //set content
        when(holder.itemViewType){
           NoteType.Text.id -> {
               holder.objectLayout.noteContent.text = mainData?.Content
           }
           NoteType.Photo.id ->{
               holder.objectLayout.noteContent.text = mainData?.Content
           }
        }
    }

    override fun getItemCount() = noteList.size

    interface  OnNoteClickListener {
         fun onNoteClick(position:Int)
    }
}