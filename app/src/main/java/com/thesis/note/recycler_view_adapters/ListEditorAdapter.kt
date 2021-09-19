package com.thesis.note.recycler_view_adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.Editable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.ListData
import com.thesis.note.databinding.RecyclerViewListItemBinding

/**
 * [RecyclerView] adapter for lists
 */
class ListEditorAdapter(listDataClass: ListData)
    :ListViewerAdapter(listDataClass)
{

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ListItemHolder {
        return ListItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_list_item, parent, false) as ConstraintLayout
            ,onListItemClickListener
        )
    }

    /**  */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ListItemHolder, position: Int) {
        val binding = RecyclerViewListItemBinding.bind(holder.objectLayout)
        //set edit text
        with(binding.editTextListItem){
            text = Editable.Factory().newEditable(listDataClass.itemsList[position].text)
            addTextChangedListener { listDataClass.itemsList[position].text = it.toString() }
        }
        //set checkbox
        with(binding.listItemCheckBox){
            isChecked = listDataClass.itemsList[position].checked
            setOnCheckedChangeListener { _, isChecked ->
                listDataClass.itemsList[position].checked = isChecked
                if(isChecked) {
                    binding.editTextListItem.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.gray_400))
                }
                else{
                    binding.editTextListItem.paintFlags = 0
                    binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.black))
                }
            }
            if(isChecked) {
                binding.editTextListItem.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.gray_400))
            }
            else{
                binding.editTextListItem.paintFlags = 0
                binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.black))
            }
        }
        //set delete button
        binding.deleteButton.setOnClickListener {
            listDataClass.itemsList.removeAt(position)
            notifyItemRemoved(position)
        }
        //set drag indicator
        binding.dragIndicator.setOnTouchListener{ _ ,event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                itemTouchHelper?.startDrag(holder)
            }
            false
        }
    }
}
