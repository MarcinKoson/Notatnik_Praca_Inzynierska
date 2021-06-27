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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout


import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.flexbox.FlexboxLayoutManager


import com.google.android.material.navigation.NavigationView
import com.thesis.note.*

import kotlinx.android.synthetic.main.template_empty_layout.navigationView
import kotlinx.android.synthetic.main.template_empty_layout.toolbar

import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.*
import com.thesis.note.recycler_view_adapters.NoteViewerAdapter
import com.thesis.note.recycler_view_adapters.TagListAdapter
import kotlinx.android.synthetic.main.activity_note_viewer.*
import kotlinx.android.synthetic.main.activity_note_viewer.deleteButton

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
//TODO TAGS

class NoteViewerActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    NoteViewerAdapter.OnNoteListener, DialogInterface.OnDismissListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer

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
    private lateinit var tagListAdapterListener: TagListAdapter.OnNoteListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setSupportActionBar(toolbar)
        setContentView(R.layout.activity_note_viewer)
        drawerLayout = activity_note_viewer_layout
        navigationDrawer = NavigationDrawer(drawerLayout)
        navigationView.setNavigationItemSelectedListener(this)

        val drawerToggle= ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.abdt,R.string.abdt)
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
                        noteName.text = Editable.Factory.getInstance().newEditable(note.Name)
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
                groupSpinner.adapter = groupArrayAdapter
                //set group of note
                val group :Group? = groupsList.firstOrNull{ x -> x.IdGroup == note.GroupID}
                if (group != null) {
                    groupSpinner.setSelection(groupsList.indexOf(group)+1)
                }
                //set date
                //TODO date
                //Tags RecyclerView init
                //TODO tag list adapter listener

                tagListAdapterListener = object: TagListAdapter.OnNoteListener {
                    override fun onNoteClick(position: Int) {
                        //create remove dialog
                        val alertDialog: AlertDialog? = this?.let {
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
                            }
                            builder.create()
                        }
                        alertDialog?.show()
                    }
                }
                tagsViewManager = FlexboxLayoutManager(noteViewerActivityContext)
                tagsViewAdapter = TagListAdapter(tagsOfNoteList,tagsList,tagListAdapterListener)
                tagsRecyclerView = findViewById<RecyclerView>(R.id.tagRecyclerView).apply {
                    //setHasFixedSize(true)
                    layoutManager = tagsViewManager
                    adapter = tagsViewAdapter
                }
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
                    db.noteDao().updateTodo(note)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        groupSpinner.onItemSelectedListener = groupOnItemClickListener

        //TODO save note name after change



        //add tag button
        addTagButton.setOnClickListener {
            val newFragment = AddTagsDialogFragment()
            val bundle = Bundle()
            bundle.putInt("noteID",noteID)
            newFragment.arguments = bundle
            newFragment.show(supportFragmentManager, "add tags")
        }
        //remove button
        deleteButton.setOnClickListener {
            GlobalScope.launch {
                val db = AppDatabase(applicationContext)
                db.noteDao().delete(note)
            }
            finish()
        }
        //share button
        //TODO share button

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

    override fun onNoteClick(position: Int) {

        when(viewAdapter.getItemViewType(position)){
            //NoteType.Text
            0 -> {
                val textEditorActivityIntent = Intent(this, TextEditorActivity::class.java)
                textEditorActivityIntent.putExtra("noteID",noteID)
                textEditorActivityIntent.putExtra("dataID",dataList[position].IdData)
                this.startActivity(textEditorActivityIntent)
            }
            else -> {
                Toast.makeText(applicationContext,"ERROR:NoteViewerActivity - cannot open data", Toast.LENGTH_SHORT).show()
            }
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
