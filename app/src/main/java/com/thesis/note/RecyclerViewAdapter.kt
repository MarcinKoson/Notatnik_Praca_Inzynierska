package com.thesis.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.R
import kotlinx.android.synthetic.main.recycler_view_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecyclerViewAdapter (private var noteSet: List<Note>, private var dataSet:List<Data>, onNoteListener: OnNoteListener) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    val mOnNoteListener = onNoteListener;

    fun changeDataset(newData: List<Note>){
        noteSet = newData
        notifyDataSetChanged()
    }

    class MyViewHolder(val objectLayout: ConstraintLayout, val listener: OnNoteListener) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{
        init{
            objectLayout.setOnClickListener(this)
        }
        val onNoteListener = listener;
        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): RecyclerViewAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_layout, parent, false) as ConstraintLayout
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(textView,mOnNoteListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        val mainData = dataSet.firstOrNull{it.IdData == noteSet[position].MainData}

        holder.objectLayout.noteName.text = noteSet[position].Name
        holder.objectLayout.noteContent.text = mainData?.Content
        holder.objectLayout.favoriteCheckBox.isChecked = noteSet[position].Favorite

        //TODO multimedialna
        val noteTypeStr: String
        noteTypeStr = when(mainData?.Type){
            NoteType.Text -> "Tekstowa"
            NoteType.List -> "Lista"
            NoteType.Video -> "Wideo"
            NoteType.Sound -> "Dziękowa"
            NoteType.Photo -> "Obraz"
            else -> "Inna"
        }
        holder.objectLayout.noteType.text = noteTypeStr

        //Sprawdzenie czy jest w grupie, jeżeli tak znalezeinie nazwy
        if(noteSet[position].GroupID !=null)
            GlobalScope.launch {
                val groupName = AppDatabase(holder.objectLayout.context).groupDao().getId(noteSet[position].GroupID!!).Name
                holder.objectLayout.groupName.text = groupName
            }


        //Listener do przycisku favorite
        holder.objectLayout.favoriteCheckBox.setOnClickListener (
            fun (_:View){
                noteSet[position].Favorite = holder.objectLayout.favoriteCheckBox.isChecked
                GlobalScope.launch{
                    AppDatabase(holder.objectLayout.context).noteDao().updateTodo(noteSet[position])
                }
            }
        )


    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = noteSet.size


    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}