package com.example.todo

import androidx.room.*

@Dao
interface ItemTagDao {
    @Insert
    suspend fun insert(itemTag: ItemTag)

    @Query("DELETE FROM item_tag_table WHERE itemId = :itemId AND tagId = :tagId")
    suspend fun deleteItemTag(itemId: Long, tagId: Long)
}
