package com.xpl0t.scany.repository.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "page", foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("document_id"),
            onDelete = CASCADE
        )
    ]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "document_id") val documentId: Int,
    @ColumnInfo(name = "order") val order: Int
)
