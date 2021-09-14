package com.thesis.note.activity

import android.os.Bundle
import com.thesis.note.DrawerActivity
import com.thesis.note.databinding.ActivitySettingsBinding

//TODO SettingsActivity
/**
 *
 */
class SettingsActivity : DrawerActivity() {

    /** View binding */

    private lateinit var binding: ActivitySettingsBinding

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
    }
}
