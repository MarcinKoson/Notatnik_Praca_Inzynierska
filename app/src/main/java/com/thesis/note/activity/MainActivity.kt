package com.thesis.note.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.thesis.note.DrawerActivity
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColor
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Data
import com.thesis.note.database.entity.Group
import com.thesis.note.database.entity.Note
import com.thesis.note.database.entity.Tag
import com.thesis.note.databinding.ActivityMainBinding
import com.thesis.note.fragment.SortNotesFragment
import com.thesis.note.SortNotesType
import com.thesis.note.fragment.AddNoteFragment
import com.thesis.note.fragment.SearchFragment
import com.thesis.note.recycler_view_adapters.NoteTilesAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 *  Main activity of application. It opens on application start.
 *
 *  You can pass extended data [SearchFragment.SearchValues]
 *  with putExtra("search", yourSearchValues.toString())
 */
class MainActivity : DrawerActivity(), SearchFragment.SearchInterface
{
    /** This activity */
    private val thisActivity = this
    /** View binding */
    lateinit var binding: ActivityMainBinding

    /** Database */
    lateinit var db: AppDatabase
    /** List of all notes */
    private lateinit var listOfNotes: List<Note>
    /** List of displayed notes */
    private var displayedListOfNotes = MutableLiveData<List<Note>>()
    /** List of data */
    private lateinit var listOfData: List<Data>
    /** List of groups */
    private lateinit var listOfGroups: List<Group>
    /** List of groups */
    private lateinit var listOfTags: List<Tag>

    /** Notes sort type */
    private var sortType: SortNotesType = SortNotesType.Date
    /** Is note sort ascending */
    private var sortAsc: Boolean = false
    /** Current search values */
    private var currentSearchValues: SearchFragment.SearchValues? = null

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase(this)
        GlobalScope.launch {
            checkFirstStart()
        }
        loadParameters()
        initRecyclerView()
        //Observer for displayedListOfNotes
        displayedListOfNotes.observe(this, { displayedListOfNotes.value?.let { runOnUiThread { updateRecyclerView(it)} } })
        //Add button listener
        binding.floatingActionButton.setOnClickListener {
            AddNoteFragment().show(supportFragmentManager,"add_note")
        }
        //Search button listener
        binding.searchButton.setOnClickListener {
            SearchFragment(this, listOfGroups,listOfTags, currentSearchValues).show(supportFragmentManager,"search")
        }
        //Search off button
        binding.searchOffButton.setOnClickListener {
            runOnUiThread {
                currentSearchValues = null
                displayedListOfNotes.value = listOfNotes
                binding.searchOffButton.visibility = View.INVISIBLE
            }
        }
        //Sort button listener
        binding.sortButton.setOnClickListener {
            SortNotesFragment(sortType,sortAsc).show(supportFragmentManager,"sort")
        }
        //SortNotesFragment listener
        supportFragmentManager.setFragmentResultListener("sort", this) { _, bundle ->
            sortListOfNotes(SortNotesType.fromInt(bundle.getInt("sortType")),bundle.getBoolean("sortAsc"))
            if(currentSearchValues != null){
                displayedListOfNotes.value = filterNotes(currentSearchValues!!)
            }
            else
            {
                displayedListOfNotes.value = listOfNotes
            }
        }
    }

    /** On resume callback */
    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            listOfGroups = db.groupDao().getAll()
            listOfTags = db.tagDao().getAll()
            loadNotes()
            sortListOfNotes(sortType,sortAsc)
            if(currentSearchValues == null){
                runOnUiThread { displayedListOfNotes.value = listOfNotes }
            }
            else{
                runOnUiThread { displayedListOfNotes.value = currentSearchValues?.let{filterNotes(it)}}
            }
        }
    }

    /** Load parameters passed from another activity */
    private fun loadParameters() {
        val parameters = intent.extras
        if (parameters != null) {
            currentSearchValues = SearchFragment.SearchValues().apply {
                parameters.getString("search")?.let { fromString(it) }
            }
        }
    }

    /** On note click listener for recycler view adapter */
    private val onNoteClickListener = object : NoteTilesAdapter.OnNoteClickListener {
        override fun onNoteClick(position: Int) {
            Intent(thisActivity, NoteViewerActivity::class.java).run{
                putExtra("noteID", listOfNotes[position].IdNote)
                thisActivity.startActivity(this)
            }
        }
    }

    /** Load [Data] and [Note] from database into [listOfData] and [listOfNotes].
     * It is accessing database
     */
    private fun loadNotes(){
        listOfData = db.dataDao().getAll()
        listOfNotes = db.noteDao().getAll()
    }

    /** Recycler view initialization. It needs [listOfNotes] and [listOfData] loaded from database. Should be running on UI thread*/
    private fun initRecyclerView(){
        binding.noteTilesRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(thisActivity, 2)
            adapter = NoteTilesAdapter(listOf(), listOf(),onNoteClickListener)
        }
    }

    /** Updates recycler view to use passed [newListOfNotes] and current [listOfData]. Should be running on UI thread */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(newListOfNotes:List<Note>){
        val viewAdapter = NoteTilesAdapter(newListOfNotes, listOfData,onNoteClickListener)
        binding.noteTilesRecyclerView.adapter = viewAdapter
        viewAdapter.notifyDataSetChanged()
    }

    /** Search fragment callback */
    override fun onSearchClick(searchValues: SearchFragment.SearchValues) {
        currentSearchValues = searchValues
        displayedListOfNotes.value = filterNotes(searchValues)
        binding.searchOffButton.visibility = View.VISIBLE
    }

    /** Filter [listOfNotes] with [searchValues] and return filtered list of notes */
    private fun filterNotes(searchValues: SearchFragment.SearchValues):List<Note>{
        var notes = listOfNotes

        if(searchValues.content != null) {
            val idNotes = listOfData
                    .filter { it.Type == NoteType.Text && it.Content.matches(Regex(".*" + searchValues.content!! + ".*")) }
                    .map { it.NoteId }
            notes = notes.filter { x -> idNotes.find { x.IdNote == it } != null }
        }

        if(searchValues.favorite)
            notes = notes.filter { it.Favorite }

        if(searchValues.noteType != null)
            notes = notes.filter { x -> listOfData.firstOrNull{ it.IdData == x.MainData }?.Type == searchValues.noteType ?: false }

        if(searchValues.group != null)
            notes = notes.filter { it.GroupID == searchValues.group }

        //TODO tag filtering

        if(searchValues.dateMin != null && searchValues.dateMax != null)
        {
            val dateMin = SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(searchValues.dateMin!!)
            val endOfDay = "-24:00"
            val dateMax = SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.US).parse(searchValues.dateMax!!+endOfDay)
            notes = notes.filter { if(it.Date!=null) it.Date!! > dateMin && it.Date!! < dateMax else false }
        }

        if(searchValues.name != null)
            notes = notes.filter { it.Name.matches(Regex(".*" + searchValues.name!! + ".*")) }

        return notes
    }

    /** Sorts [listOfNotes] in place */
    private fun sortListOfNotes(sortType: SortNotesType, sortAsc:Boolean){
        this.sortType = sortType
        this.sortAsc = sortAsc
        listOfNotes = when(sortType) {
            SortNotesType.Alphabetically ->
                if (sortAsc) listOfNotes.sortedWith { x, y -> x.Name.compareTo(y.Name) }
                else listOfNotes.sortedWith { x, y -> y.Name.compareTo(x.Name) }
            SortNotesType.Date ->
                if (sortAsc) listOfNotes.sortedWith { x, y ->
                    x.Date?.compareTo(y.Date).let { it ?: -1 }
                }
                else listOfNotes.sortedWith { x, y -> y.Date?.compareTo(x.Date).let { it ?: -1 } }
            SortNotesType.Group ->
                if (sortAsc) listOfNotes.sortedWith { x, y ->
                    val xx = listOfGroups.firstOrNull { z -> z.IdGroup == x?.GroupID }
                    val yy = listOfGroups.firstOrNull { z -> z.IdGroup == y?.GroupID }
                    when {
                        xx == null -> 1
                        yy == null -> -1
                        else -> xx.Name.compareTo(yy.Name)
                    }
                }
                else listOfNotes.sortedWith { x, y ->
                    val xx = listOfGroups.firstOrNull { z -> z.IdGroup == x?.GroupID }
                    val yy = listOfGroups.firstOrNull { z -> z.IdGroup == y?.GroupID }
                    when {
                        xx == null -> 1
                        yy == null -> -1
                        else -> yy.Name.compareTo(xx.Name)
                    }
                }
        }
    }

    /** Check if application is run for first time.
     * If it is, then add example notes, tags and groups into database */
    private fun checkFirstStart() {
        val sharedPrefs = getSharedPreferences("appSharedPrefs",MODE_PRIVATE)
        val notFirstStart = sharedPrefs.getBoolean("notFirstStart", false)
        if(!notFirstStart){
            val db = AppDatabase(thisActivity)
            db.groupDao().insertAll(Group(0,"Grupa 1",null))
            db.groupDao().insertAll(Group(0,"Grupa 2",null))
            db.groupDao().insertAll(Group(0,"Grupa 3",null))
            db.tagDao().insertAll(Tag(0,"Tag 1"))
            db.tagDao().insertAll(Tag(0,"Tag 2"))
            db.tagDao().insertAll(Tag(0,"Tag 3"))

            var note = db.noteDao().insertAll(Note(0,"Note",null,null,false,null, Date(),null,NoteColor.Cyan))
            var data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example",null,16,NoteColor.Black))
            db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

            note = db.noteDao().insertAll(Note(0,"Bold",null,null,false,null, Date(),null,NoteColor.Teal))
            data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example","B",16,NoteColor.Purple))
            db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

            note = db.noteDao().insertAll(Note(0,"Italic",null,null,false,null, Date(),null,NoteColor.Yellow))
            data = db.dataDao().insertAll(Data(0,note[0].toInt(),NoteType.Text,"example","I",16,NoteColor.Black))
            db.noteDao().update(db.noteDao().getNoteById(note[0].toInt()).apply { this.MainData = data[0].toInt() })

            loadNotes()
            displayedListOfNotes.value = listOfNotes

            sharedPrefs.edit().putBoolean("notFirstStart", true).apply()
        }
    }
}
