package com.thesis.note.recycler_view_adapters

import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.databinding.RecyclerViewLabelBinding

/**
 * [RecyclerView] adapter for lists in note viewer
 */
class LabelAdapter(private val labelList: MutableList<String>)
    :RecyclerView.Adapter<LabelAdapter.LabelHolder>() {

    /** Listener called when label is clicked */
    var onLabelClickListener: (Int) -> Unit = {}

    /** Listener called when label should be deleted */
    var onDeleteLabelListener: (Int) -> Unit = {}

    /** Listener called when label should be edited */
    var onEditLabelListener: (Int,String) -> Unit = {_,_ -> }

    /**  */
    class LabelHolder(
        val objectLayout: ConstraintLayout,
        val listener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener {

        init {
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.invoke(adapterPosition)
        }
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelHolder {
        return LabelHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_view_label,
                parent,
                false
            ) as ConstraintLayout, onLabelClickListener
        )
    }

    /**  */
    override fun onBindViewHolder(holder: LabelHolder, position: Int) {
        val binding = RecyclerViewLabelBinding.bind(holder.objectLayout)

        binding.labelText.text = labelList[position]

        binding.deleteButton.setOnClickListener {
            //create remove dialog
            AlertDialog.Builder(holder.objectLayout.context).run{
                setPositiveButton(R.string.activity_note_viewer_dialog_remove_tag_positive_button) { _, _ ->
                    labelList.removeAt(position)
                    onDeleteLabelListener.invoke(position)
                    notifyItemRemoved(position)
                }
                setNegativeButton(R.string.activity_label_dialog_remove_tag_negative_button) { _, _ -> }
                setTitle(R.string.activity_label_dialog_remove_note)
                create()
            }.show()
        }

        binding.editButton.setOnClickListener {
            binding.editButton.visibility = View.GONE
            binding.saveButton.visibility = View.VISIBLE
            binding.labelText.visibility = View.GONE
            binding.labelEdit.text = Editable.Factory().newEditable(binding.labelText.text)
            binding.labelEdit.visibility = View.VISIBLE
        }

        binding.saveButton.setOnClickListener {
            binding.labelText.text = binding.labelEdit.text.toString()
            labelList[position] = binding.labelEdit.text.toString()
            binding.labelEdit.visibility = View.GONE
            binding.labelText.visibility = View.VISIBLE
            binding.saveButton.visibility = View.GONE
            binding.editButton.visibility = View.VISIBLE
            onEditLabelListener.invoke(position,binding.labelEdit.text.toString())
        }
    }

    /**  */
    override fun getItemCount() = labelList.size

    /** */
    fun addNew(toAdd:String){
        labelList.add(toAdd)
        notifyItemInserted(labelList.size-1)
    }

}
