package com.thesis.note.database.dao

import androidx.room.*
import com.thesis.note.database.NoteType
import com.thesis.note.database.entity.Note

@Dao
interface NoteDAO {
    @Query("SELECT * FROM note")
    fun getAll(): List<Note>

    @Query("SELECT * FROM note WHERE IdNote = (:noteID)")
    fun getNoteById(noteID:Int):Note

    @Query("SELECT * FROM note WHERE Favorite=(:favorite)")
    fun getFavorite(favorite: Boolean): List<Note>

    @Insert
    fun insertAll(vararg insert: Note): List<Long>

    @Delete
    fun delete(delete: Note)

    @Update
    fun updateTodo(vararg update: Note)


  @Query("SELECT * FROM note WHERE GroupID IN(:groupsID) AND Favorite IN(:favorite) AND Name LIKE(:nameRegex)")
  fun getFilteredGroup(
      groupsID : List<Int?>,
      favorite : List<Boolean>,
      nameRegex : String = "%"
  ): List<Note>

  @Query("SELECT * FROM note WHERE Favorite IN(:favorite) AND Name LIKE(:nameRegex)")
    fun getFiltered(
        favorite : List<Boolean>,
        nameRegex : String = "%"
    ): List<Note>
    //fun getFilteredWithContent

    /*
    @Query("SELECT * FROM note JOIN tagOfNote WHERE NoteID=IdNote AND Type IN(:noteTypes)")
    fun getFilteredWithTags(

        tags : List<Int>,

        noteTypes: List<NoteType> = NoteType.values().toList()

    ):List<Note>
    */
    //fun getFilteredWithContentAngTags

}