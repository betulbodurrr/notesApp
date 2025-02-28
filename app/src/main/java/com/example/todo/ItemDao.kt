package com.example.todo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: Item):Long


    @Update
    suspend fun updateItem(item: Item)

    // Notu tamamen sil
    @Query("DELETE FROM item_table WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

}