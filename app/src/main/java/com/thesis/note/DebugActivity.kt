package com.thesis.note

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.thesis.note.activity.ImageNoteActivity
import com.thesis.note.activity.ListActivity
import com.thesis.note.activity.XTemplateEmptyActivity
import com.thesis.note.databinding.DebugActivityBinding

class DebugActivity : AppCompatActivity() {

    private lateinit var binding:DebugActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DebugActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.templatebutton.setOnClickListener{
            val template = Intent(this, XTemplateEmptyActivity::class.java)
            startActivity(template)
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



