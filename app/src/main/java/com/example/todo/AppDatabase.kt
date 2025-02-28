package com.example.todo

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class, Item::class, UserItem::class, Tag::class, ItemTag::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao
    abstract fun userItemDao(): UserItemDao
    abstract fun tagDao(): TagDao
    abstract fun itemTagDao(): ItemTagDao

}
