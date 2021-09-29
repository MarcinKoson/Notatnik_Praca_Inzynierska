package com.thesis.note.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.thesis.note.R

class SettingsImageNote : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_image_note, rootKey)
    }
}
