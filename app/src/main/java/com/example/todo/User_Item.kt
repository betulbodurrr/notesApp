package com.example.todo

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["userId", "itemId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"],onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"],onDelete = ForeignKey.CASCADE)
    ]
)
data class UserItem(
    val userId: Long,
    val itemId: Long
)
