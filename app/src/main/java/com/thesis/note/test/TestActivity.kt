package com.thesis.note.test

import android.os.Bundle
import com.thesis.note.DrawerActivity
import com.thesis.note.databinding.XActivityTestBinding

class TestActivity : DrawerActivity()  {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    private lateinit var binding: XActivityTestBinding

    /** On create callback */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = XActivityTestBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        //-----------------------------------------------------------------------

    }
}




