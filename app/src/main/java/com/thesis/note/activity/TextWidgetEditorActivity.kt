package com.thesis.note.activity

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.thesis.note.Constants
import com.thesis.note.R
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.Color
import com.thesis.note.database.ColorConverter
import com.thesis.note.database.ColorPalette
import com.thesis.note.database.entity.TextWidget
import com.thesis.note.databinding.ActivityTextWidgetEditorLayoutBinding
import com.thesis.note.fragment.ColorPickerFragment
import com.thesis.note.widget.NoteWidget
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Activity for editing text widget.
 *
 * When creating [Intent] of this activity, you can put extended data with
 * putExtra("textWidgetID", yourTextWidgetID).
 * Activity will load [TextWidget] with passed id.
 *
 */
class TextWidgetEditorActivity : DrawerActivity() {
    /** This activity */
    private val thisActivity = this

    /** View binding */
    lateinit var binding: ActivityTextWidgetEditorLayoutBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Edited [TextWidget] id */
    private var textWidgetID:Int = 0

    /** Edited [TextWidget] */
    private var editedTextWidget: TextWidget? = null

    /** Current font size */
    private var fontSize : Int = Constants.TEXT_SIZE_SMALL.toInt()

    /** Current font color */
    private var fontColor = Color.Black

    /** Current background color */
    private var backgroundColor = Color.Yellow

    /** List of size of font */
    val fontSizeList = listOf(8,12,16,21,25,27,30)

    /** On create callback. Loading data, layout init and setting listeners */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextWidgetEditorLayoutBinding.inflate(layoutInflater)
        //loadSettings()
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        db = AppDatabase.invoke(this)
        loadParameters()
        GlobalScope.launch {
            loadFromDB()
            runOnUiThread {
                setLayout()
                //listener for changes in text
                binding.editedText.doOnTextChanged { _, _, _, _ -> showDiscardChangesDialog = true }
            }
        }

        //Save button listener
        binding.saveButton.setOnClickListener {
            GlobalScope.launch {
                editedTextWidget?.also {
                    it.Content = binding.editedText.text.toString()
                    it.Color = backgroundColor
                    it.FontColor = fontColor
                    it.Size = fontSize.toFloat()
                    db.textWidgetDao().update(it)
                }
                NoteWidget.updateAllWidgets(thisActivity)

                finish()
            }
            Toast.makeText(applicationContext, R.string.activity_text_editor_save_OK, Toast.LENGTH_SHORT).show()
        }

        //Share button listener
        binding.shareButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply{
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, binding.editedText.text.toString())
                startActivity(Intent.createChooser(this, getString(R.string.activity_text_editor_share)))
                //startActivity(this)
            }
        }

        //Speech to text button listener
        binding.micButton.setOnClickListener {
            showDiscardChangesDialog = true
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                try {
                    startForResultSpeechToText.launch(this)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(thisActivity, R.string.activity_text_editor_stt_error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Text color button listener
        binding.textColorButton.setOnClickListener {
            ColorPickerFragment(ColorPalette.TEXT_COLOR_PALETTE).show(supportFragmentManager, "tag")
        }

        //ColorPickerFragment result listener
        supportFragmentManager.setFragmentResultListener("color",this) { _, bundle ->
            val result = bundle.getInt("colorID")
            fontColor = ColorConverter().intToEnum(result)!!
            binding.editedText.setTextColor(resources.getColor(ColorConverter.enumToColor(fontColor),null))
            showDiscardChangesDialog = true
        }

        //Background color button listener
        binding.backgroundColorButton.setOnClickListener {
            ColorPickerFragment(ColorPalette.TEXT_COLOR_PALETTE).show(supportFragmentManager, "widgetBackground")
        }

        //ColorPickerFragment result listener - background
        supportFragmentManager.setFragmentResultListener("widgetBackground",this) { _, bundle ->
            val result = bundle.getInt("colorID")
            backgroundColor = ColorConverter().intToEnum(result)!!
            binding.editedText.background = ResourcesCompat.getDrawable(resources, ColorConverter.enumToColor(backgroundColor), null)
            showDiscardChangesDialog = true
        }

        //Text size spinner setup
        binding.textSizeSpinner.apply {
            adapter = ArrayAdapter(thisActivity, android.R.layout.simple_spinner_item, fontSizeList).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            fontSizeList.indexOf(fontSize).let { if(it!=-1) setSelection(it)  }
            post {
                onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                        showDiscardChangesDialog = true
                        fontSize = fontSizeList[position]
                        binding.editedText.textSize = fontSizeList[position].toFloat()
                    }
                    override fun onNothingSelected(parentView: AdapterView<*>?) {}
                }
            }
        }
    }

    /** Load parameters passed from another activity */
    private fun loadParameters() {
        val parameters = intent.extras
        if (parameters != null) {
            textWidgetID = parameters.getInt("textWidgetID")
        }
    }

    /** Load [TextWidget] form database. */
    private fun loadFromDB() {
        if (textWidgetID != 0) {
            editedTextWidget = db.textWidgetDao().getTextWidgetById(textWidgetID)
            editedTextWidget?.let{
                fontSize = it.Size.toInt()
                backgroundColor = it.Color
                fontColor = it.FontColor
            }
        }
    }

    /** Set loaded [TextWidget] into layout */
    private fun setLayout(){
        if(textWidgetID != 0) {
            //show data in textField
            editedTextWidget?.Content?.let { setText(it) }
            //set font size
            binding.editedText.textSize = fontSize.toFloat()
            fontSizeList.indexOf(fontSize).let { if(it!=-1 && binding.textSizeSpinner.adapter != null) binding.textSizeSpinner.setSelection(it) }
            //set font color
            binding.editedText.setTextColor(
                resources.getColor(
                    ColorConverter.enumToColor(
                        fontColor
                    ), null
                )
            )
            editedTextWidget?.Color?.let {
                binding.editedText.background =
                    ResourcesCompat.getDrawable(resources, ColorConverter.enumToColor(it), null)
            }
        }
    }

    /** Set content into text edit */
    private fun setText(content: String){
        binding.editedText.text = Editable.Factory.getInstance().newEditable(content)
    }

    /** Callback from speech to text */
    private val startForResultSpeechToText = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultArray = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = resultArray?.get(0)
            binding.editedText.text = binding.editedText.text?.append(" $recognizedText")
        }
    }

}
