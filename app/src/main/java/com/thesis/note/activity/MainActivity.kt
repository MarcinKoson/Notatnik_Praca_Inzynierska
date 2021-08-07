package com.thesis.note.activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.thesis.note.DrawerActivity
import com.thesis.note.SearchValuesS
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Group
import com.thesis.note.database.entity.Note
import com.thesis.note.database.entity.Tag
import com.thesis.note.databinding.ActivityMainBinding
import com.thesis.note.recycler_view_adapters.NoteTilesListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  Main activity of application. It opens on application start.
 */
class MainActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityMainBinding

    /** Database */
    lateinit var db: AppDatabase

    /** List of notes */
    private lateinit var listOfNotes: List<Note>

    /** List of data */
    private lateinit var listOfData: List<Data>

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase(this)
        GlobalScope.launch {
            checkFirstStart()
            loadNotes()
            runOnUiThread{ initRecyclerView() }
        }
        //Add button listener
        //TODO add note fragment
        binding.floatingActionButton.setOnClickListener {
            Intent(it.context,AddNoteActivity::class.java).run{
                startActivity(this)
            }
        }
        //TODO search note fragment
        //Search button listener
        binding.searchButton.setOnClickListener {
            Intent(it.context,SearchActivity::class.java).run{
                startActivity(this)
            }
        }
        //TODO add note fragment
        //Sort button listener
        binding.sortButton.setOnClickListener {

        }
    }

    /** On resume callback */
    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            loadNotes()
            runOnUiThread { updateRecyclerView() }
        }
    }

    /** On note click listener for recycler view adapter */
    private val onNoteClickListener = object : NoteTilesListAdapter.OnNoteClickListener {
        override fun onNoteClick(position: Int) {
            Intent(thisActivity, NoteViewerActivity::class.java).run{
                putExtra("noteID", listOfNotes[position].IdNote)
                thisActivity.startActivity(this)
            }
        }
    }

    /** Recycler view initialization. It needs [listOfNotes] and [listOfData] loaded from database. Should be running on UI thread*/
    private fun initRecyclerView(){
        val viewManager = GridLayoutManager(thisActivity, 2)
        val viewAdapter = NoteTilesListAdapter(listOfNotes, listOfData,onNoteClickListener)
        binding.noteTilesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    /** Updates recycler view to use current [listOfNotes] and [listOfData]. Should be running on UI thread */
    private fun updateRecyclerView(){
        val viewAdapter = NoteTilesListAdapter(listOfNotes, listOfData,onNoteClickListener)
        binding.noteTilesRecyclerView.adapter = viewAdapter
        viewAdapter.notifyDataSetChanged()
    }

    /** Load [Data] and [Note] from database into [listOfData] and [listOfNotes].
     * It load only that [Note]s that meets conditions form [SearchValuesS] and all [Data] from database.
     * It is accessing database
     */
    private fun loadNotes(){
        //Load data
        listOfData = db.dataDao().getAll()
        //Search values
        val favorite: MutableList<Boolean> = if(SearchValuesS.favorite)
            mutableListOf(true)
        else
            mutableListOf(true,false)
        //Name
        var nameReg = SearchValuesS.name
        if(nameReg==null || nameReg==""){
            nameReg = "%"
        }
        //Groups
        listOfNotes = if (SearchValuesS.group == null) {
            db.noteDao().getFiltered(favorite, nameReg.toString())
        } else {
            val groups: MutableList<Int?> = mutableListOf(SearchValuesS.group)
            db.noteDao().getFilteredGroup(groups, favorite, nameReg.toString())
        }
    }

    /** Check if application is run for first time.
     * If it is, then add example notes, tags and groups into database */
    private fun checkFirstStart() {
        val sharedPrefs = getSharedPreferences("appSharedPrefs",MODE_PRIVATE)
        val notFirstStart = sharedPrefs.getBoolean("notFirstStart", false)
        if(!notFirstStart){
            GlobalScope.launch {
                val db = AppDatabase(thisActivity)
                db.groupDao().insertAll(Group(0,"Grupa 1",null))
                db.groupDao().insertAll(Group(0,"Grupa 2",null))
                db.groupDao().insertAll(Group(0,"Grupa 3",null))
                db.tagDao().insertAll(Tag(0,"Tag 1"))
                db.tagDao().insertAll(Tag(0,"Tag 2"))
                db.tagDao().insertAll(Tag(0,"Tag 3"))

                var note = db.noteDao().insertAll(Note(0,"Note",null,null,false,null,null,null,NoteColor.Cyan))
                var data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example",null,16,NoteColor.Black))
                db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

                note = db.noteDao().insertAll(Note(0,"Bold",null,null,false,null,null,null,NoteColor.Teal))
                data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example","B",16,NoteColor.Purple))
                db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

                note = db.noteDao().insertAll(Note(0,"Italic",null,null,false,null,null,null,NoteColor.Yellow))
                data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example","I",16,NoteColor.Black))
                db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })
            }
            sharedPrefs.edit().putBoolean("notFirstStart", true).apply()
        }
    }
}
