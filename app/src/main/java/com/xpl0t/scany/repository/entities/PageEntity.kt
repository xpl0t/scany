package com.xpl0t.scany.repository.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.sql.Blob

@Entity(tableName = "page", foreignKeys = [
    ForeignKey(
        entity = ScanEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("scan_id"),
        onDelete = CASCADE
    )
])
data class PageEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "scan_id") val scanId: Int,
    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB) val image: ByteArray?,
    // Next is kept for the order of pages: https://stackoverflow.com/a/14640166/14981939
    @ColumnInfo(name = "next") val next: Int?
)
