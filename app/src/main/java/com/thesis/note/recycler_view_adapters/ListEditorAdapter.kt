package com.thesis.note.recycler_view_adapters

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.ListData
import com.thesis.note.databinding.RecyclerViewListItemBinding

/**
 * [RecyclerView] adapter for lists
 */
class ListEditorAdapter(
    private val listData: ListData,
    private val onListItemClickListener: OnListItemListener,
    private val onTextChangedListener: OnTextChangedListener
    ) :RecyclerView.Adapter<ListEditorAdapter.ListItemHolder>() {

    /**  */
    interface  OnListItemListener {
        fun onListItemClick(position:Int)
    }

    /**  */
    interface  OnTextChangedListener {
        fun onTextChanged(position:Int, newText:String)
    }

    /**  */
    class ListItemHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnListItemListener
        ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{

        init{
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onListItemClick(adapterPosition)
        }
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ListItemHolder {
        return ListItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_list_item, parent, false) as ConstraintLayout
            ,onListItemClickListener
        )
    }

    /**  */
    override fun onBindViewHolder(holder: ListItemHolder, position: Int) {
        val binding = RecyclerViewListItemBinding.bind(holder.objectLayout)
        binding.editTextListItem.text = Editable.Factory().newEditable(listData.itemsList[position].text)
        binding.listItemCheckBox.isChecked = listData.itemsList[position].checked
        binding.editTextListItem.addTextChangedListener { onTextChangedListener.onTextChanged(position,it.toString()) }
    }

    /**  */
    override fun getItemCount() = listData.itemsList.size
}