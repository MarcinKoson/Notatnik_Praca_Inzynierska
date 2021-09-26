package com.thesis.note.recycler_view_adapters

import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thesis.note.R
import com.thesis.note.database.ListData
import com.thesis.note.database.ColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.*
import com.thesis.note.SoundPlayer

/**
 * [RecyclerView] adapter for showing [Data] of [Note]
 */
class NoteViewerAdapter (
    private var dataList:List<Data>,
    private var onDataClickListener: OnDataClickListener
    ) :RecyclerView.Adapter<NoteViewerAdapter.DataHolder>() {

    /**  */
    interface  OnDataClickListener {
        fun onDataClick(position:Int)
    }

    /**  */
    class DataHolder(
        val objectLayout: ConstraintLayout,
        val listener: OnDataClickListener
        ) : RecyclerView.ViewHolder(objectLayout), View.OnClickListener{

        init{
            objectLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            listener.onDataClick(adapterPosition)
        }

        var soundPlayer : SoundPlayer? = null

    }

    /**  */
    override fun getItemViewType(position: Int): Int {
        return dataList[position].Type.id
    }

    /**  */
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): DataHolder {
        return when(viewType){
            NoteType.Text.id -> {
                DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_text, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            NoteType.List.id -> {
                DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_list, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            NoteType.Image.id -> {
                 DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_image, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            NoteType.Recording.id -> {
                 DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_recording, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
            else -> {
                DataHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_note_viewer_text, parent, false) as ConstraintLayout
                    ,onDataClickListener)
            }
        }
    }

    /**  */
    override fun onBindViewHolder(holder: DataHolder, position: Int) {
       when(holder.itemViewType){
           NoteType.Text.id -> setTextData(holder, position)
           NoteType.List.id -> setListData(holder, position)
           NoteType.Image.id -> setImageData(holder,position)
           NoteType.Recording.id -> setRecordingData(holder, position)
           else -> {
               val binding = RecyclerViewNoteViewerTextBinding.bind(holder.objectLayout)
               binding.noteViewerTextContent.text = holder.objectLayout.context.getString(R.string.activity_note_viewer_error_cannot_open)
           }
       }
    }

    /**  */
    private fun setTextData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerTextBinding.bind(holder.objectLayout)
        binding.noteViewerTextContent.text = dataList[position].Content
        binding.noteViewerTextContent.setTextColor(
            holder.itemView.resources.getColor(
                ColorConverter.enumToColor(dataList[position].Color),null
            )
        )
        binding.noteViewerTextContent.textSize = dataList[position].Size?.toFloat()!!
        when(dataList[position].Info){
            "B" -> binding.noteViewerTextContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            "I" -> binding.noteViewerTextContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            "BI" -> binding.noteViewerTextContent.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        }
    }

    /**  */
    private fun setListData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerListBinding.bind(holder.objectLayout)
        //Init RecyclerView
        val viewManager = LinearLayoutManager(binding.root.context)
        val viewAdapter = ListViewerAdapter(ListData().apply { loadData(dataList[position])}).apply {
            attachItemTouchHelperToRecyclerView(binding.listItemsRecyclerView)
        }
        binding.listItemsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        listRecyclerViews[position] = binding.listItemsRecyclerView
        binding.editButton.setOnClickListener { onDataClickListener.onDataClick(position) }
    }

    /**  */
    private fun setImageData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerImageBinding.bind(holder.objectLayout)
        Glide.with(holder.itemView)
            .load(dataList[position].Content)
            .fitCenter()
            .placeholder(R.drawable.ic_loading)
            .into(binding.noteViewerImage)
    }

    /**  */
    private fun setRecordingData(holder: DataHolder, position: Int){
        val binding = RecyclerViewNoteViewerRecordingBinding.bind(holder.objectLayout)
        binding.editButton.setOnClickListener { onDataClickListener.onDataClick(position) }

        holder.soundPlayer = SoundPlayer(holder.objectLayout.context).apply {
            currentPositionTextView = binding.timeNow
            durationTextView = binding.allTime
            handler = Handler(Looper.getMainLooper())
            openFile(dataList[position].Content)
        }

        binding.playButton.setOnClickListener { holder.soundPlayer?.play()}
        binding.pauseButton.setOnClickListener { holder.soundPlayer?.pause() }
        binding.stopButton.setOnClickListener { holder.soundPlayer?.stop() }
    }

    /** On view recycled callback */
    override fun onViewRecycled(holder: DataHolder) {
        super.onViewRecycled(holder)
        holder.soundPlayer?.release()
    }

    /**  */
    override fun getItemCount() = dataList.size

    /** */
    private var listRecyclerViews:MutableMap<Int,RecyclerView> = mutableMapOf()

    /** */
    fun getRecyclerView(position:Int): RecyclerView? {
        return listRecyclerViews[position]
    }

}
