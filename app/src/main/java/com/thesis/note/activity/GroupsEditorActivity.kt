package com.thesis.note.activity

import android.content.Intent
import com.thesis.note.database.entity.Group
import com.thesis.note.fragment.SearchFragment

/**
 * Activity for editing [Group]s
 */
class GroupsEditorActivity : LabelEditorActivity<Group>(){

    override fun deleteT(toDelete: Group) {
            db.groupDao().delete(toDelete)
    }

    override fun updateT(toUpdate: Group, newString: String) {
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

    override fun getName(value: Group): String {
        return value.Name
    }

    override fun addNewT(toAdd: String): Group {
        return db.groupDao().getId(db.groupDao().insertAll(Group(0,toAdd,null))[0].toInt())
    }
}
