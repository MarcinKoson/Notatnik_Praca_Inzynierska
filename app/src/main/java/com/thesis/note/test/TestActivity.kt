package com.thesis.note.test

import android.R.attr.minDate
import android.R.attr.startYear
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thesis.note.databinding.XActivityTestBinding


class TestActivity : AppCompatActivity() {

    private lateinit var binding: XActivityTestBinding
    private lateinit var contextThis: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = XActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testAAA.setOnClickListener {

        }


    }

}




