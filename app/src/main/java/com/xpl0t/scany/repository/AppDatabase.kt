package com.xpl0t.scany.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xpl0t.scany.repository.dao.PageDao
import com.xpl0t.scany.repository.dao.DocumentDao
import com.xpl0t.scany.repository.entities.PageEntity
import com.xpl0t.scany.repository.entities.DocumentEntity

@Database(entities = [DocumentEntity::class, PageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun pageDao(): PageDao
}
