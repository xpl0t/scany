package com.xpl0t.scany.repository.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "page", foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("scan_id"),
            onDelete = CASCADE
        )
    ]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "scan_id") val scanId: Int,
    @ColumnInfo(name = "order") val order: Int
)
