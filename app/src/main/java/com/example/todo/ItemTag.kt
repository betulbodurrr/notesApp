package com.example.todo

import androidx.room.*

@Entity(tableName = "item_tag_table",
    foreignKeys = [
        ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["tagId"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ])
data class ItemTag(
    @PrimaryKey(autoGenerate = true) val itemTagId: Long = 0,
    val itemId: Long,
    val tagId: Long
)
