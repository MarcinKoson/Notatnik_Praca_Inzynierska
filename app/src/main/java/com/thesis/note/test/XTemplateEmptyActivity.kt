package com.thesis.note.test

import android.os.Bundle
import com.thesis.note.activity.DrawerActivity
import com.thesis.note.databinding.XTemplateEmptyLayoutBinding

/**
 * Template for activity with drawer. Change [XTemplateEmptyLayoutBinding] to binding class of your layout.
 */
class XTemplateEmptyActivity : DrawerActivity() {

    /** View binding */
    private lateinit var binding: XTemplateEmptyLayoutBinding //CHANGE TO YOUR LAYOUT BINDING CLASS

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = XTemplateEmptyLayoutBinding.inflate(layoutInflater) //CHANGE TO YOUR LAYOUT BINDING CLASS
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
    }
}

