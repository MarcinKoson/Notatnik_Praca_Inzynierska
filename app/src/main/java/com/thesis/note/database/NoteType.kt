package com.thesis.note.database

/**
 * Enum of note types
 */
enum class NoteType(val id:Int){
    Text(0),
    List(1),
    Image(2),
    Recording(3)
}
