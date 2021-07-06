package com.thesis.note

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.thesis.note.activity.ImageNoteActivity
import kotlinx.android.synthetic.main.activity_debug.*

class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val intentTest = Intent(this, ImageNoteActivity::class.java)
        intentTest.putExtra("dataID", -1)
        intentTest.putExtra("noteID", -1)
                testActivity.setOnClickListener(object: View.OnClickListener {
                    override fun onClick(v: View?) {
                       startActivity(intentTest)
                    }})


        /*
        //DB testing
        dbButton.setOnClickListener(object: OnClickListener {
            override fun onClick(v: View?) {

                val db = AppDatabase(applicationContext)
                GlobalScope.launch {
                  //  db.noteDao().insertAll(Note("aaa", 0))
                    val all = db.noteDao().getAll()
                    val widgets = db.widgetDao().getAll()
                    val group = db.groupDao().getAll()
                   // val test = db.noteDao().getFilteredWithTags(listOf(1,2))
                    val all2 = db.noteDao().getAll()
                   // db.noteDao().insertAll(Note("bbb", 0))
                }
            }
        })
        //DB remove
        deleteDBbutton.setOnClickListener(object: OnClickListener{
            override fun onClick(v: View?) {
                val db = Room.databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java, "testDB.db"
                ).build()
                GlobalScope.launch {
                db.clearAllTables();

                    }
            }
        })
        //TextEditor testing
        val intent = Intent(this, TextEditorActivity::class.java)
        textEditorButton.setOnClickListener(object: OnClickListener {
            override fun onClick(v: View?) {
                startActivity(intent)
        }})
        //Template open
        val intent2 = Intent(this, TemplateEmptyActivity::class.java)
        templatebutton.setOnClickListener(object: OnClickListener {
            override fun onClick(v: View?) {
                startActivity(intent2)
            }})


        addGropus.setOnClickListener(object :OnClickListener{
            override fun onClick(v: View?) {
                GlobalScope.launch {
                val db = AppDatabase(applicationContext)
                db.groupDao().insertAll(
                    Group(0, "Grupa 1", null),
                    Group(0, "Grupa 2", null),
                    Group(0, "Grupa 3", null)
                )
                val all = db.groupDao().getAll()
                val all2 = db.groupDao().getAll()
            }
            }
        })
        //DB search test
        searchTest.setOnClickListener(object :OnClickListener{
            override fun onClick(v: View?) {
                GlobalScope.launch {
                    val db = AppDatabase(applicationContext)

                    val groups:MutableList<Int?> = db.groupDao().getAll().map { it.IdGroup }.toMutableList();
                    groups.add(null);

                    val favorite:MutableList<Boolean> = mutableListOf();
                    favorite.add(true)
                    favorite.add(false)

                    val full = db.noteDao().getAll()
                    val a = db.noteDao().getFiltered(groupsID = groups, favorite = favorite, nameRegex = "%")
                    val b = db.noteDao().getFiltered(groupsID = groups, favorite = favorite, nameRegex = "%")
                }
            }
        })

        //voice text activity
        val intent3 = Intent(this, VoiceToTextActivity::class.java)
        voicetext.setOnClickListener(object: OnClickListener {
            override fun onClick(v: View?) {
                startActivity(intent3)
            }})
*/
    }
}



