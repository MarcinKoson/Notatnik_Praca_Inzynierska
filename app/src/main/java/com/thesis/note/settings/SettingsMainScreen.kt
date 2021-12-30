package com.thesis.note.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.thesis.note.R
import com.thesis.note.fragment.AboutFragment

class SettingsMainScreen : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val about = this.findPreference<Preference>("about")
        about?.setOnPreferenceClickListener {
            this.activity?.supportFragmentManager?.let{sfm -> AboutFragment().show(sfm,"") }
            true
        }
    }
}
