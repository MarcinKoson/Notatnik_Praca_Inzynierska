package com.thesis.note.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.navigation.NavigationView
import com.thesis.note.*
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteColorConverter
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.*
import com.thesis.note.databinding.ActivityNoteViewerBinding
import com.thesis.note.fragment.AddTagsDialogFragment
import com.thesis.note.fragment.ChooseColorFragment
import com.thesis.note.recycler_view_adapters.NoteViewerAdapter
import com.thesis.note.recycler_view_adapters.TagListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//TODO documentation
class NoteViewerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NoteViewerAdapter.OnDataClickListener, DialogInterface.OnDismissListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityNoteViewerBinding

    lateinit var db: AppDatabase

    private val noteViewerActivityContext = this

    private var parameters: Bundle? = null
    var noteID:Int = -1
    private lateinit var note:Note
    private lateinit var dataList: List<Data>
    private lateinit var groupsList: List<Group>
    private lateinit var tagsList: List<Tag>
    private lateinit var tagsOfNoteList: List<TagOfNote>

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var tagsRecyclerView: RecyclerView
    private lateinit var tagsViewAdapter: RecyclerView.Adapter<*>
    private lateinit var tagsViewManager: RecyclerView.LayoutManager
    private lateinit var tagListAdapterListener: TagListAdapter.OnTagClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.activityNoteViewerLayout
        navigationDrawer = NavigationDrawer(drawerLayout)
        binding.navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------
        db = AppDatabase(this)
        //Load Note
        parameters = intent.extras
        if(parameters == null){
            Toast.makeText(applicationContext,"ERROR: cannot load note", Toast.LENGTH_SHORT).show()
        }
        else{
            GlobalScope.launch {
                noteID = parameters!!.getInt("noteID")
                //Loading from db
                note = db.noteDao().getNoteById(noteID)
                dataList = db.dataDao().getDataFromNote(noteID)
                groupsList = db.groupDao().getAll()
                tagsList = db.tagDao().getAll()
                tagsOfNoteList = db.tagOfNoteDAO().getAllNoteTags(note.IdNote)
                //Setting note name
                noteViewerActivityContext.runOnUiThread(
                    fun(){
                        binding.noteName.text = Editable.Factory.getInstance().newEditable(note.Name)
                    }
                )
                //Data RecyclerView init
                viewManager = LinearLayoutManager(noteViewerActivityContext)
                viewAdapter = NoteViewerAdapter(dataList,noteViewerActivityContext)
                recyclerView = findViewById<RecyclerView>(R.id.note_viewer_recycler_view).apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
                //load spinner of groups
                val arrayGroupsString = groupsList.map { x -> x.Name }
                val groupArrayAdapter : ArrayAdapter<String> =  ArrayAdapter<String>(noteViewerActivityContext,
                    android.R.layout.simple_spinner_item,
                    arrayGroupsString)
                groupArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                groupArrayAdapter.insert(getString(R.string.groups_without_group),0)
                binding.groupSpinner.adapter = groupArrayAdapter
                //set group of note
                val group :Group? = groupsList.firstOrNull{ x -> x.IdGroup == note.GroupID}
                if (group != null) {
                    binding.groupSpinner.setSelection(groupsList.indexOf(group)+1)
                }
                //set date
                //TODO date
                //Tags RecyclerView onClick listener
                tagListAdapterListener = object: TagListAdapter.OnTagClickListener {
                    override fun onNoteClick(position: Int) {
                        //create remove dialog
                        val alertDialog: AlertDialog = this.let {
                            val builder = AlertDialog.Builder(noteViewerActivityContext)
                            builder.apply {
                                setPositiveButton("Tak",
                                    DialogInterface.OnClickListener { dialog, id ->
                                        GlobalScope.launch {
                                            db.tagOfNoteDAO().delete(tagsOfNoteList[position])
                                            //TODO unsafe?
                                            onDismiss(null)
                                        }
                                    })
                                setNegativeButton("Nie",
                                    DialogInterface.OnClickListener { dialog, id ->

                                    })
                                setTitle("Czy usunąć tag?")
                            }
                            builder.create()
                        }
                        alertDialog?.show()
                    }
                }
                //Tags RecyclerView init
                tagsViewManager = FlexboxLayoutManager(noteViewerActivityContext)
                tagsViewAdapter = TagListAdapter(tagsOfNoteList,tagsList,tagListAdapterListener)
                tagsRecyclerView = findViewById<RecyclerView>(R.id.tagRecyclerView).apply {
                    //setHasFixedSize(true)
                    layoutManager = tagsViewManager
                    adapter = tagsViewAdapter
                }
                //Background color
                binding.root.background = ResourcesCompat.getDrawable(resources,NoteColorConverter().enumToColor(note.Color),null)
            }
        }
        //save Group after change
        val groupOnItemClickListener =   object:AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,view: View?, pos: Int, id: Long
            ) {
                GlobalScope.launch {
                    if(pos == 0){
                        note.GroupID = null
                    }
                    else {
                        note.GroupID = groupsList[pos - 1].IdGroup
                    }
                    db.noteDao().update(note)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        binding.groupSpinner.onItemSelectedListener = groupOnItemClickListener
        //add tag button

        binding.tagButton.setOnClickListener {
            val newFragment = AddTagsDialogFragment()
            val bundle = Bundle()
            bundle.putInt("noteID",noteID)
            newFragment.arguments = bundle
            newFragment.show(supportFragmentManager, "add tags")
        }
        //remove button
        binding.deleteButton.setOnClickListener {
            GlobalScope.launch {
                val db = AppDatabase(applicationContext)
                db.noteDao().delete(note)
            }
            finish()
        }
        //share button
        //TODO share button
        //background color
        binding.backgroundColorButton.setOnClickListener {
            ChooseColorFragment().show(supportFragmentManager,"tag")
        }
        supportFragmentManager.setFragmentResultListener("color",this) { _, bundle ->
            val result = bundle.getString("colorID")
            val colorID = result?.toInt()
            note.Color = NoteColorConverter().intToEnum(colorID)!!
            binding.root.background = ResourcesCompat.getDrawable(resources,NoteColorConverter().enumToColor(note.Color),null)
            GlobalScope.launch {
                db.noteDao().update(note)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //update dataList
        if(parameters!=null) {
            GlobalScope.launch {
                //load data form db
                dataList = db.dataDao().getDataFromNote(noteID)
                //set new data to recycler view
                viewAdapter = NoteViewerAdapter(dataList, noteViewerActivityContext)
                runOnUiThread{
                    recyclerView.adapter = viewAdapter
                    viewAdapter.notifyDataSetChanged()
                }}}}

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDataClick(position: Int) {

        when(viewAdapter.getItemViewType(position)){
            //NoteType.Text
            NoteType.Text.id  -> {
                val textEditorActivityIntent = Intent(this, TextEditorNewActivity::class.java).apply{
                    putExtra("noteID",noteID)
                    putExtra("dataID",dataList[position].IdData)
                }
                this.startActivity(textEditorActivityIntent)
            }
            NoteType.Photo.id -> {
                val imageNoteIntent = Intent(this, ImageNoteActivity::class.java)
                imageNoteIntent.putExtra("dataID", dataList[position].IdData)
                imageNoteIntent.putExtra("noteID", noteID)
                startActivity(imageNoteIntent)
            }
            NoteType.Sound.id -> {
                val soundNoteIntent = Intent(this, SoundEditorActivity::class.java)
                soundNoteIntent.putExtra("dataID", dataList[position].IdData)
                soundNoteIntent.putExtra("noteID", noteID)
                startActivity(soundNoteIntent)
            }
            else -> {
                Toast.makeText(applicationContext,"ERROR:NoteViewerActivity - cannot open data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause(){
        super.onPause()
        //save name of note after change
        //TODO is this good solution?
        GlobalScope.launch {
            note.Name = binding.noteName.text.toString()
            db.noteDao().update(note)
        }
    }
    override fun onDismiss(dialog: DialogInterface?) {
        //on dismiss DialogFragment
        //update tag list
        if(parameters!=null) {
            GlobalScope.launch {
                //load data form db
                tagsOfNoteList = db.tagOfNoteDAO().getAllNoteTags(noteID)
                //set new data to recycler view
                tagsViewAdapter = TagListAdapter(tagsOfNoteList,tagsList,tagListAdapterListener)
                runOnUiThread{
                    tagsRecyclerView.adapter = tagsViewAdapter
                    tagsViewAdapter.notifyDataSetChanged()
                }}}
    }
    }
