package com.xpl0t.scany.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xpl0t.scany.repository.dao.PageDao
import com.xpl0t.scany.repository.dao.ScanDao
import com.xpl0t.scany.repository.entities.PageEntity
import com.xpl0t.scany.repository.entities.ScanEntity

@Database(entities = [ScanEntity::class, PageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun pageDao(): PageDao
}
