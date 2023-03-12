package com.xpl0t.scany.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.xpl0t.scany.repository.entities.PageEntity
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface PageDao {

    @Query("SELECT id, document_id, `order` FROM page")
    fun getAll(): Observable<List<PageEntity>>

    @Query("SELECT id, document_id, `order` FROM page WHERE document_id = :documentId")
    fun getByDocumentId(documentId: Int): Observable<List<PageEntity>>

    @Query("SELECT id, document_id, `order` FROM page WHERE document_id = :documentId ORDER BY `order` DESC LIMIT 1")
    fun getLastPage(documentId: Int): Single<List<PageEntity>>

    @Query("SELECT * FROM page WHERE id = :pageId")
    fun getPageImage(pageId: Int): Single<PageEntity>

    @Insert
    fun insert(vararg page: PageEntity): Single<List<Long>>

    @Query("UPDATE page SET `order` = :next WHERE id = :pageId")
    fun updateOrder(pageId: Int, next: Int?): Single<Int>

    @Query("DELETE FROM page WHERE id = :pageId")
    fun delete(pageId: Int): Single<Int>

    @Query("DELETE FROM page WHERE document_id = :documentId")
    fun deleteByDocumentId(documentId: Int): Single<Int>
}