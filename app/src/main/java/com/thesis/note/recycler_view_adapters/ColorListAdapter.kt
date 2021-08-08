package com.thesis.note.recycler_view_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.thesis.note.R
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.databinding.RecyclerViewColorPickerBinding

/**
 * [RecyclerView] adapter for showing list of colors
 */
class ColorListAdapter (
    private val colorList:List<NoteColor>,
    private val onColorClickListener: OnColorClickListener
    ) :RecyclerView.Adapter<ColorListAdapter.ColorHolder>() {

    /**  */
    interface  OnColorClickListener {
        fun onColorClick(position:Int)
    }

    /**  */
    class ColorHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnColorClickListener
        ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{

        init{
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onColorClick(adapterPosition)
        }
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ColorHolder {
        return ColorHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_color_picker, parent, false) as ConstraintLayout
            ,onColorClickListener
        )
    }

    /**  */
    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        val binding = RecyclerViewColorPickerBinding.bind(holder.objectLayout)
        binding.colorChooseButton.setOnClickListener{
            onColorClickListener.onColorClick(position)
        }
        binding.colorChooseButton.backgroundTintList =
            holder.itemView.resources.getColorStateList(NoteColorConverter.enumToColor(colorList[position]),null)
    }

    /**  */
    override fun getItemCount() = colorList.size
}
