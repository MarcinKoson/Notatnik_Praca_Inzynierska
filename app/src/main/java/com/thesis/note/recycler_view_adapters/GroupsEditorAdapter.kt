package com.thesis.note.recycler_view_adapters

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Group


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.widget.PopupWindow
import android.widget.LinearLayout

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.inputmethod.InputMethodManager

import android.view.*
import com.thesis.note.R
import com.thesis.note.databinding.PopupEditGroupBinding
import com.thesis.note.databinding.RecyclerViewAddTagFragmentBinding
import com.thesis.note.databinding.RecyclerViewGropusLayoutBinding

//TODO context??
//TODO join with TagListAdapter??
class GroupsEditorAdapter (private var myDataset: List<Group>, onNoteListener: OnNoteListener, var context:Context) :
    RecyclerView.Adapter<GroupsEditorAdapter.MyViewHolder>() {

    val mOnNoteListener = onNoteListener;

    fun changeDataset(newData: List<Group>){
        myDataset = newData
        notifyDataSetChanged()
    }

    class MyViewHolder(val textView: ConstraintLayout, val listener: OnNoteListener) : RecyclerView.ViewHolder(textView), View.OnClickListener{
        val binding = RecyclerViewGropusLayoutBinding.bind(textView)
        init{
            textView.setOnClickListener(this)
        }
        val onNoteListener = listener;



        override fun onClick(v: View?) {
            onNoteListener.onNoteClick(adapterPosition);
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_gropus_layout, parent, false) as ConstraintLayout
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(textView,mOnNoteListener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.binding.tagName.text = myDataset[position].Name

        holder.binding.deleteButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {



                GlobalScope.launch {
                    val db = AppDatabase(context)
                    db.groupDao().delete(myDataset[position])

                    (context as Activity).runOnUiThread{
                        Toast.makeText(context,"USUNIETO", Toast.LENGTH_SHORT).show()
                        (context as Activity).recreate()
                    }

                }
        }})

        holder.binding.editTagButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                (context as Activity).runOnUiThread {
                    //TODO fix
                    val inflater = (context as Activity).getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

                    val popupView = inflater.inflate(R.layout.popup_edit_group, null)
                    val bindingPopup = PopupEditGroupBinding.bind(popupView)

                    bindingPopup.newGroupName.setText(myDataset[position].Name)
                    bindingPopup.newGroupName.setSelection( bindingPopup.newGroupName.text.length)

                    bindingPopup.newGroupName.setOnKeyListener(object: View.OnKeyListener{
                        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                            if (event?.getAction()!=KeyEvent.ACTION_DOWN){
                                //TODO do funkcji
                                GlobalScope.launch {
                                    var db = AppDatabase(context)
                                    var newGroup = myDataset[position]
                                    newGroup.Name = bindingPopup.newGroupName.text.toString()
                                    db.groupDao().update(newGroup)
                                    (context as Activity).runOnUiThread{
                                        (context as Activity).recreate()}
                                }
                            }
                            return true
                        }
                    })




                    bindingPopup.saveGroupButton.setOnClickListener(object: View.OnClickListener {
                        override fun onClick(v: View?) {
                            //TODO do funkcji
                        GlobalScope.launch {
                            var db = AppDatabase(context)
                            var newGroup = myDataset[position]
                            newGroup.Name = bindingPopup.newGroupName.text.toString()
                            db.groupDao().update(newGroup)
                            (context as Activity).runOnUiThread{
                            (context as Activity).recreate()}
                        }
                    }})

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