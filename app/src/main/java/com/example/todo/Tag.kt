package com.example.todo

import androidx.room.*

@Entity(tableName = "tag_table")
data class Tag(
    @PrimaryKey(autoGenerate = true) val tagId: Long = 0,
    val tagName: String
)
val initialTags = listOf("Ders Notu", "Önemli Notlar", "Film")