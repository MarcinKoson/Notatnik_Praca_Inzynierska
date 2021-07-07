package com.thesis.note

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        button10.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {

                val newFragment = AddTagsDialogFragment()
                newFragment.show(supportFragmentManager, "test")
            }
    })
}}
