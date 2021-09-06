package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.ListData
import com.thesis.note.database.NoteColor
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Note
import com.thesis.note.databinding.ActivityListEditorLayoutBinding
import com.thesis.note.recycler_view_adapters.ListEditorAdapter
import com.thesis.note.recycler_view_adapters.ListEditorAdapter.OnListItemListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 *  Activity for list editing.
 *
 * When creating [Intent] of this activity, you should put extended data with
 * putExtra("noteID", yourNoteID) and putExtra("dataID", yourDataID).
 * If passed id equals "-1" activity interprets this as new data or new note.
 * Default value for [noteID] and [dataID] is "-1".
 *
 */
//TODO documentation
class ListEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityListEditorLayoutBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [Data] id */
    private var dataID:Int = -1
    /** Edited [ListData] */
    private var listData = ListData()
    /** Edited [Note]  id */
    private var noteID:Int = -1

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListEditorLayoutBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase.invoke(this)
        loadParameters()
        listData.itemsList.add(ListData.ListItem())
        GlobalScope.launch {
            if(dataID != -1)
                loadData()
            runOnUiThread { initRecyclerView() }
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
            when {
                dataID != -1 -> {
                    //update
                    GlobalScope.launch {
                        db.dataDao().update(listData.getData())
                        db.noteDao().update(db.noteDao().getNoteById(listData.noteID).apply { Date = Date() })
                    }
                }
                noteID != -1 -> {
                    //add new data to db
                    GlobalScope.launch {
                        db.dataDao().insertAll(listData.run{ listData.idData=0 ; getData() })
                        db.noteDao().update(db.noteDao().getNoteById(listData.noteID).apply { Date = Date() })
                    }
                }
                else -> {
                    GlobalScope.launch {
                        //add new note
                        db.noteDao().insertAll(Note(0, "", null, null, false, null, Date(), null, NoteColor.White)).also {
                            noteID = it[0].toInt()
                            listData.noteID = noteID
                        }
                        db.dataDao().insertAll(listData.run{ listData.idData=0 ; getData() }).also{
                            dataID = it[0].toInt()
                            db.noteDao().update(db.noteDao().getNoteById(noteID).apply{ MainData = dataID })
                        }
                        //open new note
                        runOnUiThread {
                            thisActivity.startActivity(Intent(thisActivity, NoteViewerActivity::class.java).apply{putExtra("noteID", noteID)})
                        }
                    }
                }
            }
            Toast.makeText(applicationContext, R.string.activity_list_editor_save_OK, Toast.LENGTH_SHORT).show()
            finish()
        }

        //TODO Delete button listener
        binding.deleteButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //TODO Share button listener
        binding.shareButton.setOnClickListener {
            Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        }

        //Add list item button listener
        binding.addListItemButton.setOnClickListener {
            listData.itemsList.add(ListData.ListItem())
            binding.listRecyclerView.adapter?.notifyItemInserted(listData.itemsList.size-1)
        }
    }

    /** Load parameters passed from another activity */
    private fun loadParameters() {
        val parameters = intent.extras
        if (parameters != null) {
            dataID = parameters.getInt("dataID")
            noteID = parameters.getInt("noteID")
        }
    }

    /** Recycler view initialization. Should be running on UI thread*/
    private fun initRecyclerView(){
        //TODO look for better solution for maxHeight
        with(thisActivity.resources.displayMetrics){
            binding.recyclerViewLayout.maxHeight = heightPixels - (130 * density).toInt()
        }
        val viewManager = FlexboxLayoutManager(thisActivity)
        val viewAdapter = ListEditorAdapter(listData,
            object : OnListItemListener {
                override fun onListItemClick(position: Int) {

                }
            }
        ,
            object : ListEditorAdapter.OnTextChangedListener{
                override fun onTextChanged(position: Int, newText: String) {
                    listData.itemsList[position].text = newText
                }
            }
        )
        binding.listRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
        ItemTouchHelper(itemTouchHelperCallback).apply{
            attachToRecyclerView(binding.listRecyclerView)
        }
    }

    /** Load [Data] with id [dataID] from database  */
    private fun loadData(){
        listData = ListData().apply {
            loadData(db.dataDao().getDataById(dataID))
        }
    }

    /**  */
    private val itemTouchHelperCallback = object: ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            // Specify the directions of movement
            return makeMovementFlags(UP or DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            // Notify your adapter that an item is moved from x position to y position
            binding.listRecyclerView.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun isLongPressDragEnabled(): Boolean {
            // true: if you want to start dragging on long press
            // false: if you want to handle it yourself
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }

    }

}

