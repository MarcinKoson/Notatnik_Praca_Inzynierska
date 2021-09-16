package com.thesis.note.recycler_view_adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.ListData
import com.thesis.note.databinding.RecyclerViewNoteViewerListItemBinding

/**
 * [RecyclerView] adapter for lists in note viewer
 */
open class ListViewerAdapter(protected val listDataClass: ListData)
    :RecyclerView.Adapter<ListViewerAdapter.ListItemHolder>() {

    /**  */
    interface OnListItemClickListener {
        fun onListItemClick(position: Int)
    }

    /**  */
    var onListItemClickListener: OnListItemClickListener = object : OnListItemClickListener {
        override fun onListItemClick(position: Int) {}
    }

    /**  */
    class ListItemHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnListItemClickListener
    ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener {

        init {
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onListItemClick(adapterPosition)
        }
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemHolder {
        return ListItemHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_note_viewer_list_item,
                parent,
                false
            ) as ConstraintLayout, onListItemClickListener
        )
    }

    /**  */
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ListItemHolder, position: Int) {
        val binding = RecyclerViewNoteViewerListItemBinding.bind(holder.objectLayout)
        //set text
        binding.listItemText.text = listDataClass.itemsList[position].text
        //set checkbox
        with(binding.listItemCheckBox) {
            isChecked = listDataClass.itemsList[position].checked
            setOnCheckedChangeListener { _, isChecked ->
                listDataClass.itemsList[position].checked = isChecked
                if (isChecked) {
                    binding.listItemText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    binding.listItemText.setTextColor(
                        ContextCompat.getColor(
                            holder.objectLayout.context,
                            R.color.gray_400
                        )
                    )
                } else {
                    binding.listItemText.paintFlags = 0
                    binding.listItemText.setTextColor(
                        ContextCompat.getColor(
                            holder.objectLayout.context,
                            R.color.black
                        )
                    )
                }
            }
            if (isChecked) {
                binding.listItemText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                binding.listItemText.setTextColor(
                    ContextCompat.getColor(
                        holder.objectLayout.context,
                        R.color.gray_400
                    )
                )
            } else {
                binding.listItemText.paintFlags = 0
                binding.listItemText.setTextColor(
                    ContextCompat.getColor(
                        holder.objectLayout.context,
                        R.color.black
                    )
                )
            }
        }
        //set dragIndicator
        binding.dragIndicator.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                itemTouchHelper?.startDrag(holder)
            }
            false
        }
    }

    /**  */
    override fun getItemCount() = listDataClass.itemsList.size

    /**  */
    fun attachItemTouchHelperToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    /**  */
    protected var recyclerView: RecyclerView? = null

    /**  */
    protected var itemTouchHelper: ItemTouchHelper? = null

    /**  */
    private val itemTouchHelperCallback = object : ItemTouchHelper.Callback() {
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
            recyclerView.adapter?.notifyItemMoved(
                viewHolder.adapterPosition,
                target.adapterPosition
            )
            listDataClass.moveListItem(viewHolder.adapterPosition, target.adapterPosition)
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

    /**  */
    fun getListData(): ListData {
        return listDataClass
    }
}
