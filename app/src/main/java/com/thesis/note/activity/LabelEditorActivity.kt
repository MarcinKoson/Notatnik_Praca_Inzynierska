package com.thesis.note.activity

import android.os.Bundle
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.DrawerActivity
import com.thesis.note.database.AppDatabase
import com.thesis.note.databinding.ActivityLabelEditorBinding
import com.thesis.note.fragment.AddLabelFragment
import com.thesis.note.recycler_view_adapters.LabelAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//TODO colors
/**
 *  Abstract class for editing labels
 */
abstract class LabelEditorActivity<T> : DrawerActivity() {
    /** This activity */
    lateinit var thisActivity: LabelEditorActivity<T>

    /** View binding */
    private lateinit var binding: ActivityLabelEditorBinding

    /** Database */
    lateinit var db: AppDatabase

    /** Current list of labels */
    private lateinit var listOfLabels : MutableList<T>

    /** Adapter for recycler view */
    private lateinit var labelAdapter: LabelAdapter

    /** On create callback */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        thisActivity = this
        db = AppDatabase.invoke(this)
        binding = ActivityLabelEditorBinding.inflate(layoutInflater)
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        GlobalScope.launch {
            listOfLabels = loadList()
            binding.labelRecyclerView.apply{
                layoutManager = FlexboxLayoutManager(thisActivity)
                labelAdapter = LabelAdapter(listOfLabels.map { getName(it) }.toMutableList()).apply {
                    onLabelClickListener = { thisActivity.onLabelClickListener(listOfLabels[it]) }
                    onEditLabelListener = {position,newString -> thisActivity.onEditLabelListener(position,newString) }
                    onDeleteLabelListener = { thisActivity.onDeleteLabelListener(it) }
                }
                adapter = labelAdapter
            }
        }
        //Add button listener
        binding.floatingActionButton.setOnClickListener {
            AddLabelFragment().show(supportFragmentManager,"add")
        }
        //AddLabelFragment listener
        supportFragmentManager.setFragmentResultListener("newLabel",this){_, bundle ->
            GlobalScope.launch {
                bundle.getString("name")?.let { name ->
                    addNewT(name).also {
                        listOfLabels.add(it)
                        runOnUiThread { labelAdapter.addNew(getName(it)) }
                    }
                }
            }
        }
    }

    /** callback called when label is deleted */
    private fun onDeleteLabelListener(position: Int) {
        GlobalScope.launch {
            deleteT(listOfLabels[position])
            listOfLabels.removeAt(position)
        }
    }

    /** callback called when label is edited */
    private fun onEditLabelListener(position: Int, newString: String) {
        GlobalScope.launch {
            updateT(listOfLabels[position], newString)
        }
    }

    /** Delete [toDelete] from db */
    abstract fun deleteT(toDelete: T)

    /** Add new label to db with name [toAdd]. Return label from db */
    abstract fun addNewT(toAdd: String) : T

    /** Update [toUpdate] to new name [newString] */
    abstract fun updateT(toUpdate:T , newString: String)

    /** Callback called when label is clicked */
    abstract fun onLabelClickListener(toOpen: T)

    /** Load all labels from db */
    abstract fun loadList():MutableList<T>

    /** Get name from label, that will be shown in recycler view */
    abstract fun getName(value:T):String

}

