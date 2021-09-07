package com.thesis.note.recycler_view_adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.Editable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.ListData
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.databinding.RecyclerViewListItemBinding

/**
 * [RecyclerView] adapter for lists
 */
class ListEditorAdapter(
    private val listData: ListData) :RecyclerView.Adapter<ListEditorAdapter.ListItemHolder>() {

    /**  */
    interface  OnListItemClickListener {
        fun onListItemClick(position:Int)
    }

    /**  */
    var onListItemClickListener: OnListItemClickListener = object: OnListItemClickListener{override fun onListItemClick(position: Int) {}}

    /**  */
    interface  OnTextChangedListener {
        fun onTextChanged(position:Int, newText:String)
    }

    /**  */
    var onTextChangedListener = object: OnTextChangedListener{override fun onTextChanged(position: Int, newText: String) {}}

    /**  */
    interface  OnCheckBoxChangedListener {
        fun onCheckBoxChanged(position:Int, isChecked:Boolean)
    }

    /**  */
    var onCheckBoxChangedListener = object: OnCheckBoxChangedListener{override fun onCheckBoxChanged(position: Int, isChecked: Boolean) {}}

    /**  */
    interface  OnDeleteButtonClickListener {
        fun onDeleteButtonClick(position:Int)
    }

    /**  */
    var onDeleteButtonClickListener = object: OnDeleteButtonClickListener{override fun onDeleteButtonClick(position: Int) {}}

    /**  */
    class ListItemHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnListItemClickListener
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
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ListItemHolder, position: Int) {
        val binding = RecyclerViewListItemBinding.bind(holder.objectLayout)
        binding.editTextListItem.text = Editable.Factory().newEditable(listData.itemsList[position].text)
        binding.editTextListItem.addTextChangedListener { onTextChangedListener.onTextChanged(position,it.toString()) }

        binding.listItemCheckBox.isChecked = listData.itemsList[position].checked
        binding.listItemCheckBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckBoxChangedListener.onCheckBoxChanged(position,isChecked)
            if(isChecked) {
                binding.editTextListItem.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.gray_400))
            }
            else{
                binding.editTextListItem.paintFlags = 0
                binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.black))
            }
        }
        if(binding.listItemCheckBox.isChecked) {
            binding.editTextListItem.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.gray_400))
        }
        else{
            binding.editTextListItem.paintFlags = 0
            binding.editTextListItem.setTextColor(getColor(holder.objectLayout.context,R.color.black))
        }

        binding.deleteButton.setOnClickListener {
            onDeleteButtonClickListener.onDeleteButtonClick(position)
        }

        binding.dragIndicator.setOnTouchListener{ _ ,event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                itemTouchHelper?.startDrag(holder)
            }
            false
        }
    }

    /**  */
    override fun getItemCount() = listData.itemsList.size

    /**  */
    fun attachItemTouchHelperToRecyclerView(recyclerView: RecyclerView){
        this.recyclerView = recyclerView
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback).apply{
            attachToRecyclerView(recyclerView)
        }
    }

    /**  */
    private var recyclerView : RecyclerView? = null

    /**  */
    private var itemTouchHelper : ItemTouchHelper? = null

    /**  */
    private val itemTouchHelperCallback = object: ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            recyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            //TODO move to clearView
            listData.moveListItem(viewHolder.adapterPosition,target.adapterPosition)
            return true
        }


        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {

        }

    }
}
