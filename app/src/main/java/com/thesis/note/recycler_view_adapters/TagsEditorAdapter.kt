package com.thesis.note.recycler_view_adapters

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.database.AppDatabase

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.widget.PopupWindow
import android.widget.LinearLayout

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.inputmethod.InputMethodManager

import android.view.*
import com.thesis.note.R
import com.thesis.note.database.entity.Tag
import com.thesis.note.databinding.PopupEditGroupBinding
import com.thesis.note.databinding.RecyclerViewTagsEditorLayoutBinding

//TODO context??
class TagsEditorAdapter (private var myDataset: List<Tag>, onNoteListener: OnNoteListener, var context:Context) :
    RecyclerView.Adapter<TagsEditorAdapter.MyViewHolder>() {

    private val mOnNoteListener = onNoteListener

    fun changeDataset(newData: List<Tag>){
        myDataset = newData
        notifyDataSetChanged()
    }

    class MyViewHolder(val textView: ConstraintLayout, listener: OnNoteListener) : RecyclerView.ViewHolder(textView), View.OnClickListener{
        init{
            textView.setOnClickListener(this)
        }
        val onNoteListener = listener

        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {

        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_tags_editor_layout, parent, false) as ConstraintLayout

        return MyViewHolder(textView,mOnNoteListener)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = RecyclerViewTagsEditorLayoutBinding.bind(holder.itemView)
        binding.tagName.text = myDataset[position].Name

        binding.deleteButton.setOnClickListener {
            GlobalScope.launch {
                val db = AppDatabase(context)
                db.tagDao().delete(myDataset[position])

                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "USUNIETO", Toast.LENGTH_SHORT).show()
                    (context as Activity).recreate()
                }

            }
        }

        binding.editTagButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                (context as Activity).runOnUiThread {
                    //TODO fix
                    val inflater = (context as Activity).getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val popupView = inflater.inflate(R.layout.popup_edit_group, null)
                    val bindingPopup = PopupEditGroupBinding.bind(popupView)

                    bindingPopup.newGroupName.setText(myDataset[position].Name)
                    bindingPopup.newGroupName.setSelection( bindingPopup.newGroupName.text.length)

                    bindingPopup.newGroupName.setOnKeyListener { v, keyCode, event ->
                        if (event?.action != KeyEvent.ACTION_DOWN) {
                            //TODO do funkcji
                            GlobalScope.launch {
                                val db = AppDatabase(context)
                                val newGroup = myDataset[position]
                                newGroup.Name = bindingPopup.newGroupName.text.toString()
                                db.tagDao().updateTodo(newGroup)
                                (context as Activity).runOnUiThread {
                                    (context as Activity).recreate()
                                }
                            }
                        }
                        true
                    }




                    bindingPopup.saveGroupButton.setOnClickListener { //TODO do funkcji
                        GlobalScope.launch {
                            val db = AppDatabase(context)
                            val newTag = myDataset[position]
                            newTag.Name = bindingPopup.newGroupName.text.toString()
                            db.tagDao().updateTodo(newTag)
                            (context as Activity).runOnUiThread {
                                (context as Activity).recreate()
                            }
                        }
                    }

                    val width = LinearLayout.LayoutParams.WRAP_CONTENT
                    val height = LinearLayout.LayoutParams.WRAP_CONTENT
                    val focusable = true // lets taps outside the popup also dismiss it
                    val popupWindow = PopupWindow(popupView, width, height, focusable)

                    bindingPopup.newGroupName.requestFocus()
                    val imm = (context as Activity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm!!.toggleSoftInput(
                        InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY
                    )
                    popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0)
                }
            }
        })


    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size


    interface  OnNoteListener {
         fun onNoteClick(position:Int)
    }
}