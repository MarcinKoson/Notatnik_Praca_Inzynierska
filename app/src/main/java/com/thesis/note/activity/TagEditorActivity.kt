package com.thesis.note.activity

import android.content.Intent
import com.thesis.note.database.entity.Tag
import com.thesis.note.fragment.SearchFragment

/**
 *  Activity for editing [Tag]s
 */
class TagEditorActivity : LabelEditorActivity<Tag>() {

    override fun deleteLabel(toDelete: Tag) {
        db.tagDao().delete(toDelete)
    }

    override fun updateLabel(toUpdate: Tag, newString: String) {
        db.tagDao().update(toUpdate.apply { Name = newString })
    }

    override fun onLabelClickListener(toOpen: Tag) {
        Intent(thisActivity, MainActivity::class.java).run {
            putExtra("search", SearchFragment.SearchValues().let { it.tag = toOpen.IdTag; it.toString() })
            thisActivity.startActivity(this)
        }
    }

    override fun loadList(): MutableList<Tag> {
        return db.tagDao().getAll().toMutableList()
    }

    override fun getName(label: Tag): String {
        return label.Name
    }

    override fun addNewLabel(toAdd: String): Tag {
        return db.tagDao().getId(db.tagDao().insertAll(Tag(0,toAdd))[0].toInt())
    }

}
