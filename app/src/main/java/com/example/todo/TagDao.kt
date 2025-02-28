package com.example.todo

import androidx.room.*

@Dao
interface TagDao {
    @Insert
    suspend fun insert(tag: Tag): Long

    @Query("SELECT * FROM tag_table")
    suspend fun getAllTags(): List<Tag>

    @Query("SELECT * FROM tag_table WHERE tagName = :tagName")
    suspend fun getTagByName(tagName: String): Tag?

    @Delete
    suspend fun delete(tag: Tag)
}
