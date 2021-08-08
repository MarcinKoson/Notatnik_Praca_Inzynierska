package com.thesis.note.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thesis.note.activity.MainActivity
import com.thesis.note.databinding.XActivityDebugBinding

class DebugActivity : AppCompatActivity() {

    private lateinit var binding:XActivityDebugBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        //Toast.makeText(applicationContext, "debug", Toast.LENGTH_SHORT).show()
        super.onCreate(savedInstanceState)
        binding = XActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.templatebutton.setOnClickListener{
            val template = Intent(this, XTemplateEmptyActivity::class.java)
            startActivity(template)
        }

        binding.testButton.setOnClickListener{
            val test = Intent(this, MainActivity::class.java)
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



