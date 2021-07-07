package com.thesis.note.activity

import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.thesis.note.NavigationDrawer

import com.thesis.note.database.AppDatabase
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Note
import com.google.android.material.navigation.NavigationView

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.thesis.note.database.entity.Group
import com.thesis.note.R
import com.thesis.note.database.entity.Data
import com.thesis.note.databinding.ActivityTextEditorOldBinding
import com.thesis.note.databinding.XTemplateEmptyLayoutBinding

@Deprecated(message = "use TextEditorActivity insted")
class TextEditorActivityOld : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawer_layout: DrawerLayout
    lateinit var navigationDrawer : NavigationDrawer
    private lateinit var binding: ActivityTextEditorOldBinding


    var noteExistInDB:Boolean = false;
    var noteID:Int = -1;

    val TextEditorActivityContext = this;

    lateinit var editedNote: Note;
    lateinit var dataNote: List<Data>

    val db = AppDatabase.invoke(this)

    var isWidget: Boolean = false
    var widgetID:Int = -1

    lateinit var groupsList:List<Group>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditorOldBinding.inflate(layoutInflater) //LAYOUT BINDING CLASS
        setContentView(binding.root)
        drawer_layout = binding.activityTextEditorLayoutOld
        navigationDrawer = NavigationDrawer(drawer_layout)
        binding.navigationView.setNavigationItemSelectedListener(this);

        val drawerToggle= ActionBarDrawerToggle(this,drawer_layout,binding.toolbar,R.string.abdt,R.string.abdt)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()
        //------------------------------------------------------------------------------------------


        //SAVE button
        binding.saveButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                if(noteExistInDB==false){
                    GlobalScope.launch {
                    var db = AppDatabase.invoke(v!!.context)
/*
                     var groupID:Int? = when(groupSpinner.selectedItemPosition){
                         0 -> null
                         1 -> 1
                         2 -> 2
                         3 -> 3
                         else -> null
                     }
*/
                    var groupID:Int?
                    if(binding.groupSpinnerOld.selectedItemPosition == 0){
                        groupID = null
                    }
                        else
                    {
                        groupID = groupsList[binding.groupSpinnerOld.selectedItemPosition].IdGroup
                    }


                    //var id = db.noteDao().insertAll(Note(0,nameNote.text.toString(),NoteType.Text,textField.text.toString(),groupID,false,null,null,null))

                        var id = db.noteDao().insertAll(Note(0,binding.nameNote.text.toString(),null,groupID,false,null,null,null))

                    noteID = id[0].toInt();
                        noteExistInDB = true;

                        var addedData = db.dataDao().insertAll(Data(0,noteID,NoteType.Text,binding.textField.text.toString(),null))
                        //db.dataDao().updateTodo(id[0])




                    GlobalScope.launch{
                        editedNote = db.noteDao().getNoteById(noteID);
                        editedNote.MainData = addedData[0].toInt()
                        db.noteDao().updateTodo(editedNote)

                            dataNote = db.dataDao().getDataFromNote(editedNote.IdNote)
                    }

                        /*
                        if(isWidget){
                            db.widgetDao().insertAll(Widget(0,noteID,widgetID))
                            TestWidget.Notes = db.noteDao().getAll()
                            TestWidget.Widgets = db.widgetDao().getAll()
                        }
*/
                    }
                }
                else{
                    //editedNote.Content = textField.getText().toString()
                        dataNote[0].Content = binding.textField.getText().toString()

                    editedNote.Name = binding.nameNote.text.toString()

                    if(binding.groupSpinnerOld.selectedItemPosition == 0){
                        editedNote.GroupID = null
                    }
                    else
                    {
                        editedNote.GroupID  = groupsList[binding.groupSpinnerOld.selectedItemPosition].IdGroup
                    }

                    GlobalScope.launch {
                        db.noteDao().updateTodo(editedNote)
                        db.dataDao().updateTodo(dataNote[0])

                        /*
                        if(isWidget){

                            TestWidget.Notes = db.noteDao().getAll()
                            TestWidget.Widgets = db.widgetDao().getAll()
                        }

                         */
                    }

                }

                /*
                //WIDGET UPDATE

                val intent = Intent(v?.context, TestWidget::class.java)
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
                // since it seems the onUpdate() is only fired on that:
                val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application,TestWidget::class.java))
                    //.getAppWidgetI‌​ds(ComponentName(getApplication(), MyAppWidgetProvider::class.java!!))
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(intent)

                    */


                Toast.makeText(applicationContext,"ZAPISANO", Toast.LENGTH_SHORT).show()
                finish()
            }}
        )
        //Remove button
        binding.deleteButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(noteExistInDB)
                GlobalScope.launch {
                    val db = AppDatabase(applicationContext)
                    db.noteDao().delete(editedNote)
                }
                finish()
            }
        })


        //spinner : Groups
        val spinner: Spinner = binding.groupSpinnerOld

        /*
        ArrayAdapter.createFromResource(
            this,
            R.array.groups_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
*/
        GlobalScope.launch {
            var db = AppDatabase.invoke(TextEditorActivityContext);
            var arrayGroups = db.groupDao().getAll();
            groupsList = arrayGroups
            var arrayGroupsString = arrayGroups.map { x -> x.Name }

            var groupArrayAdapter :ArrayAdapter<String> =  ArrayAdapter<String>(TextEditorActivityContext,
                android.R.layout.simple_spinner_item,
                arrayGroupsString);

                groupArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            groupArrayAdapter.insert(getString(R.string.groups_without_group),0)
                spinner.adapter = groupArrayAdapter

        }

        //4 spinners
        val s1a: Spinner = binding.s1
        ArrayAdapter.createFromResource(this, R.array.font_name, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            s1a.adapter = adapter
        }
        val s2a: Spinner = binding.s2
        ArrayAdapter.createFromResource(this, R.array.font_size, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            s2a.adapter = adapter
        }
        val s3a: Spinner = binding.s3
        ArrayAdapter.createFromResource(this, R.array.font_color, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            s3a.adapter = adapter
        }
        val s4a: Spinner = binding.s4
        ArrayAdapter.createFromResource(this, R.array.background_color, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            s4a.adapter = adapter
        }



        //Check if new note or edit of existing
        val parameters = intent.extras

        if (parameters != null)
        {

            noteID = parameters.getInt("noteID")
            if(noteID == 0){
                noteExistInDB = false
            }
            else {
                //load note
                GlobalScope.launch {
                    editedNote = db.noteDao().getNoteById(noteID)

                    dataNote = db.dataDao().getDataFromNote(editedNote.IdNote)

                    TextEditorActivityContext.runOnUiThread(
                        fun(){
                            binding.textField.text = Editable.Factory.getInstance().newEditable(dataNote[0].Content)
                            binding.nameNote.text = Editable.Factory.getInstance().newEditable(editedNote.Name)

                        }
                    )
                    //textField.text = Editable.Factory.getInstance().newEditable(editedNote.Content)
                }
            }

            /*
            widgetID = parameters.getInt("widgetID")
            if(widgetID!=0){
                isWidget = true
                noteID = parameters.getInt("noteID")
                if(noteID == 0){
                    noteExistInDB = false
                }
                else {
                    //load note
                    GlobalScope.launch {
                        editedNote = db.noteDao().getNoteById(noteID)
                        TextEditorActivityContext.runOnUiThread(
                            fun(){
                                textField.text = Editable.Factory.getInstance().newEditable(editedNote.Content)
                                nameNote.text = Editable.Factory.getInstance().newEditable(editedNote.Name)

                            }
                        )
                        //textField.text = Editable.Factory.getInstance().newEditable(editedNote.Content)
                    }
                }
            }
            else{
                noteExistInDB = true
                noteID = parameters.getInt("noteID")
                GlobalScope.launch {
                    editedNote = db.noteDao().getNoteById(noteID)
                    TextEditorActivityContext.runOnUiThread(
                        fun(){
                            textField.text = Editable.Factory.getInstance().newEditable(editedNote.Content)
                            nameNote.text = Editable.Factory.getInstance().newEditable(editedNote.Name)

                        }
                    )
                    //textField.text = Editable.Factory.getInstance().newEditable(editedNote.Content)
                }
            }
*/
        }
        else
        {
            noteExistInDB = false
        }



    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        finish()
        return navigationDrawer.onNavigationItemSelected(menuItem,this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
