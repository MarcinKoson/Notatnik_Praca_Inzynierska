package com.thesis.note.activity

import android.os.Bundle
import com.google.android.flexbox.FlexboxLayoutManager
import com.thesis.note.database.AppDatabase
import com.thesis.note.databinding.ActivityLabelEditorBinding
import com.thesis.note.fragment.AddLabelFragment
import com.thesis.note.recycler_view_adapters.LabelAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  Abstract class for editing labels.
 */
abstract class LabelEditorActivity<T> : DrawerActivity() {
    /** This activity. Is initiated in [onCreate]. */
    lateinit var thisActivity: LabelEditorActivity<T>

    /** View binding. Is initiated in [onCreate]. */
    lateinit var binding: ActivityLabelEditorBinding

    /** Database. Is initiated in [onCreate]. */
    lateinit var db: AppDatabase

    /** Current list of labels */
    private lateinit var listOfLabels : MutableList<T>

    /** Adapter for recycler view */
    private lateinit var labelAdapter: LabelAdapter

    /** On create callback.
     * It init variables, set layout, load labels and init recycler view, sets listeners for buttons and fragments. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init
        thisActivity = this
        db = AppDatabase.invoke(this)
        binding = ActivityLabelEditorBinding.inflate(layoutInflater)
        //set layout
        setDrawerLayout(binding.root,binding.toolbar,binding.navigationView)
        //load list of labels
        GlobalScope.launch {
            listOfLabels = loadList()
            //init recycler view
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
        //Add button listener. Opens [AddLabelFragment].
        binding.floatingActionButton.setOnClickListener {
            AddLabelFragment().show(supportFragmentManager,"add")
        }
        //AddLabelFragment result listener. It adds new label to database
        supportFragmentManager.setFragmentResultListener("newLabel",this){_, bundle ->
            GlobalScope.launch {
                bundle.getString("name")?.let { name ->
                    addNewLabel(name).also {
                        listOfLabels.add(it)
                        runOnUiThread { labelAdapter.addNew(getName(it)) }
                    }
                }
            }
        }
    }

    /** Callback called when label is deleted. It removes label from database */
    private fun onDeleteLabelListener(position: Int) {
        GlobalScope.launch {
            deleteLabel(listOfLabels[position])
            listOfLabels.removeAt(position)
        }
    }

    /** Callback called when label is edited. It updates name of label in database. */
    private fun onEditLabelListener(position: Int, newString: String) {
        GlobalScope.launch {
            updateLabel(listOfLabels[position], newString)
        }
    }

    /** Delete [toDelete] from database */
    abstract fun deleteLabel(toDelete: T)

    /** Add new label to database with name [toAdd]. Return label from database */
    abstract fun addNewLabel(toAdd: String) : T

    /** Update [toUpdate] to new name [newString] */
    abstract fun updateLabel(toUpdate:T, newString: String)

    /** Callback called when label is clicked */
    abstract fun onLabelClickListener(toOpen: T)

    /** Load all labels from database */
    abstract fun loadList():MutableList<T>

    /** Get name from label, that will be shown in recycler view */
    abstract fun getName(label:T):String

}
