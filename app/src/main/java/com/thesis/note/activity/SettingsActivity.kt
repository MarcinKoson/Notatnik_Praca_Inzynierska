package com.thesis.note.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.thesis.note.R
import com.thesis.note.databinding.ActivitySettingsBinding

/**
 *
 */
class SettingsActivity : DrawerActivity(),  PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    /** View binding */
    private lateinit var binding: ActivitySettingsBinding

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    /** On pause callback */
    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    /** Callback that changes GUI of [SettingsActivity] when preferences are changed. Updates language, theme and navigation drawer */
    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when(key){
            "theme" -> {
                setActivityTheme(sharedPreferences.getString("theme", ""))
                recreate()
            }
            "language" -> {
                sharedPreferences.getString("language", "")?.let { setLocale(it) }
            }
            "navigation_drawer_favorites" -> {
                binding.navigationView.menu.findItem(R.id.favorites).also { item ->
                    with(sharedPreferences.getBoolean("navigation_drawer_favorites", true)) {
                        item.isEnabled = this
                        item.isVisible = this
                    }
                }
            }
            "navigation_drawer_add" -> {
                binding.navigationView.menu.findItem(R.id.add_note).also { item ->
                    with(sharedPreferences.getBoolean("navigation_drawer_add", true)) {
                        item.isEnabled = this
                        item.isVisible = this
                    }
                }
            }
            "navigation_drawer_groups" -> {
                binding.navigationView.menu.findItem(R.id.groups).also { item ->
                    with(sharedPreferences.getBoolean("navigation_drawer_groups", true)) {
                        item.isEnabled = this
                        item.isVisible = this
                    }
                }
            }
            "navigation_drawer_tags" -> {
                binding.navigationView.menu.findItem(R.id.tags).also { item ->
                    with(sharedPreferences.getBoolean("navigation_drawer_tags", true)) {
                        item.isEnabled = this
                        item.isVisible = this
                    }
                }
            }
            else -> {}
        }
    }

    /** Callback from [PreferenceFragmentCompat] to open another [PreferenceFragmentCompat]*/
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        // Instantiate the new Fragment
        val newFragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment).apply{
            arguments = pref.extras
            //setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingsFragmentContainerView, newFragment)
            .addToBackStack(null)
            .commit()
        return true
    }

}
