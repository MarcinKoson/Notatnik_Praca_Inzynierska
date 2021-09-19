package com.thesis.note.activity

import android.os.Bundle
import com.thesis.note.DrawerActivity
import com.thesis.note.R
import com.thesis.note.databinding.ActivitySettingsBinding
import com.thesis.note.fragment.SettingsFragment

//TODO language changing
//TODO Theme changing
//TODO GUI settings

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

        /*
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsFragmentContainerView, SettingsFragment())
            .commit()
*/

    }
}
