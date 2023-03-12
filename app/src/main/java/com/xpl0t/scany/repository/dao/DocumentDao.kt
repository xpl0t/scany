package com.xpl0t.scany.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.xpl0t.scany.repository.entities.DocumentEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface DocumentDao {
    @Query("SELECT * FROM document")
    fun getAll(): Observable<List<DocumentEntity>>

    @Query("SELECT * FROM document WHERE id = :documentId")
    fun get(documentId: Int): Observable<DocumentEntity>

    @Query("SELECT * FROM document LIMIT 1 OFFSET :rowId")
    fun getByIdx(rowId: Long): Single<DocumentEntity>

    @Query("SELECT count(*) FROM document")
    fun getCount(): Single<Int>

    @Insert
    fun insert(vararg documents: DocumentEntity): Single<List<Long>>

    @Update
    fun update(document: DocumentEntity): Single<Int>

    @Query("DELETE FROM document WHERE id = :documentId")
    fun delete(documentId: Int): Single<Int>
}