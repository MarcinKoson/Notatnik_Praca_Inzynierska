package com.thesis.note.database

import com.thesis.note.database.entity.Data

/** Class for working with [Data] of [NoteType.List] */
class ListData {

    /** Id of data in db */
    var idData:Int = -1

    /** Id of note */
    var noteID:Int = -1

    /** Size */
    var size: Int? = null

    /** Note color */
    var color: Color? = null

    /** Class with info about single list item. */
    class ListItem{
        /**  */
        var text:String = ""
        /**  */
        var checked:Boolean = false
    }

    /** List of list items. */
    var itemsList :MutableList<ListItem> = mutableListOf()

    /** Load [Data] to this object. */
    fun loadData(data: Data){
        idData = data.IdData
        noteID = data.NoteId
        size = data.Size
        color = data.Color

        itemsList = mutableListOf()
        val splittedContent = data.Content.split("/n")
        for (i in 0..splittedContent.size-2)
        {
            itemsList.add(ListItem().apply {
                text = splittedContent[i]
                checked = data.Info?.get(i) == 'T'
            })
        }
    }

    /** Create [Data] from this object. */
    fun getData(): Data{
        var content = ""
        var info = ""
        itemsList.forEach{
            content+= it.text
            content+= "/n"
            info+= if(it.checked) "T" else "F"
        }
        return Data(idData,noteID, NoteType.List,content,info, size, color)
    }

    /** Move list item at [formPosition] to [toPosition] */
    fun moveListItem(formPosition:Int, toPosition: Int){
        val itemToMove = itemsList[formPosition]
        itemsList.removeAt(formPosition)
        itemsList.add(toPosition,itemToMove)
    }
}
