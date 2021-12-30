package com.thesis.note.test

import android.content.Intent
import android.os.Bundle
import com.thesis.note.activity.DrawerActivity
import com.thesis.note.activity.ListEditorActivity
import com.thesis.note.databinding.XActivityDebugBinding

class DebugActivity : DrawerActivity()  {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    private lateinit var binding: XActivityDebugBinding

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        //Toast.makeText(applicationContext, R.string.not_implemented, Toast.LENGTH_SHORT).show()
        //Toast.makeText(applicationContext, "debug", Toast.LENGTH_SHORT).show()
        super.onCreate(savedInstanceState)
        binding = XActivityDebugBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        //-----------------------------------------------------------------------
        binding.templateButton.setOnClickListener{
            Intent(this, TemplateEmptyActivity::class.java).run{
                startActivity(this)
            }

        }
        //-----------------------------------------------------------------------
        binding.testButton.setOnClickListener{
            Intent(this, ListEditorActivity::class.java).run{
                startActivity(this)
            }
        }
        //-----------------------------------------------------------------------
    }

}
