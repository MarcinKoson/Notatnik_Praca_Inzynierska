package com.thesis.note.fragment

enum class SortNotesType(val id:Int) {
    Alphabetically(1),
    Date(2),
    Group(3);

    companion object {
        fun fromInt(value: Int) = values().first{ x -> x.id == value }
    }

}