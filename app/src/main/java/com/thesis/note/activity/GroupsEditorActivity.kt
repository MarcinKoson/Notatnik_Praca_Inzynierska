package com.thesis.note.activity

import android.content.Intent
import com.thesis.note.database.entity.Group
import com.thesis.note.fragment.SearchFragment

/**
 * Activity for editing [Group]s
 */
class GroupsEditorActivity : LabelEditorActivity<Group>(){

    override fun deleteLabel(toDelete: Group) {
        db.groupDao().delete(toDelete)
    }

    override fun updateLabel(toUpdate: Group, newString: String) {
        db.groupDao().update(toUpdate.apply { Name = newString })
    }

    override fun onLabelClickListener(toOpen: Group) {
        Intent(thisActivity, MainActivity::class.java).run {
            putExtra("search", SearchFragment.SearchValues().let { it.group = toOpen.IdGroup; it.toString() })
            thisActivity.startActivity(this)
        }
    }

    override fun loadList(): MutableList<Group> {
        return db.groupDao().getAll().toMutableList()
    }

    override fun getName(label: Group): String {
        return label.Name
    }

    override fun addNewLabel(toAdd: String): Group {
        return db.groupDao().getId(db.groupDao().insert(Group(0,toAdd,null)).toInt())
    }

}
