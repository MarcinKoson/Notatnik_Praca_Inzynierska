package com.thesis.note.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thesis.note.activity.SoundEditorActivity
import com.thesis.note.activity.XTemplateEmptyActivity
import com.thesis.note.databinding.ActivitySoundEditorBinding
import com.thesis.note.databinding.XActivityDebugBinding

class DebugActivity : AppCompatActivity() {

    private lateinit var binding:XActivityDebugBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = XActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.templatebutton.setOnClickListener{
            val template = Intent(this, XTemplateEmptyActivity::class.java)
            startActivity(template)
        }

        binding.testbutton.setOnClickListener{
            val test = Intent(this, TestActivity::class.java)
            startActivity(test)
        }

        binding.button9.setOnClickListener {
            val test = Intent(this, SoundEditorActivity::class.java)
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



