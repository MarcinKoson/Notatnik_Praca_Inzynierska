package com.thesis.note.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.room.Database
import com.thesis.note.R
import com.thesis.note.activity.SoundEditorActivity
import com.thesis.note.activity.TextEditorNewActivity
import com.thesis.note.activity.XTemplateEmptyActivity
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Data
import com.thesis.note.databinding.ActivitySoundEditorBinding
import com.thesis.note.databinding.XActivityDebugBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DebugActivity : AppCompatActivity() {

    private lateinit var binding:XActivityDebugBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = XActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Toast.makeText(applicationContext, "debug", Toast.LENGTH_SHORT).show()
        binding.templatebutton.setOnClickListener{
            val template = Intent(this, XTemplateEmptyActivity::class.java)
            startActivity(template)
        }


        var db = AppDatabase.invoke(this)
        var list : List<Data> = listOf()
        GlobalScope.launch {
            list = db.dataDao().getAll()
        }
        binding.testbutton.setOnClickListener{
            //val test = Intent(this, TestActivity::class.java)
            //startActivity(test)

          //  Toast.makeText(applicationContext, list[0].Info, Toast.LENGTH_SHORT).show()
        }

        binding.button9.setOnClickListener {
            val test = Intent(this, TextEditorNewActivity::class.java).apply{
                putExtra("noteID", -1)
                putExtra("dataID", -1)
            }
            startActivity(test)

        }
        /*
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
        */

    }
}



