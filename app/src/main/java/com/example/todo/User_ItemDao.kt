package com.example.todo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserItemDao {
    @Insert
    suspend fun insert(userItem: UserItem)

    @Query("SELECT * FROM UserItem WHERE userId = :userId AND itemId = :itemId")
    suspend fun checkIfUserItemExists(userId: Long, itemId: Long): UserItem?

    @Query("SELECT * FROM item_table WHERE id IN (SELECT itemId FROM UserItem WHERE userId = :userId)")
    suspend fun getItemsForUser(userId: Long): List<Item>

    @Query("DELETE FROM UserItem WHERE userId = :userId AND itemId = :itemId")
    suspend fun deleteUserItem(userId: Long, itemId: Long)

    // Kullanıcı ID'sine göre ilişkili tüm UserItem verilerini silme
    @Query("DELETE FROM UserItem WHERE userId = :userId")
    suspend fun deleteUserItemByUserId(userId: Long)


    // Belirli bir notun kimlerle paylaşıldığını almak için sorgu ekliyoruz
    @Query("""
        SELECT u.* FROM user_table u
        INNER JOIN UserItem ui ON u.id = ui.userId
        WHERE ui.itemId = :itemId
    """)
    suspend fun getUsersForItem(itemId: Long): List<User>


    @Query("DELETE FROM UserItem WHERE itemId = :itemId")
    suspend fun deleteUserItemByItemId(itemId: Long)


}
