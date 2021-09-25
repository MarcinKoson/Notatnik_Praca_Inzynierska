package com.thesis.note.test

import android.os.Bundle
import com.thesis.note.activity.DrawerActivity
import com.thesis.note.databinding.XTemplateEmptyLayoutBinding

/**
 * Template for activity with drawer.
 */
class TemplateEmptyActivity : DrawerActivity() {

    /** View binding */
    private lateinit var binding: XTemplateEmptyLayoutBinding

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = XTemplateEmptyLayoutBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
    }
}

