package com.xpl0t.scany.repository.dao

import androidx.room.*
import com.xpl0t.scany.repository.entities.PageEntity
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.combineLatest
import io.reactivex.rxjava3.core.Single

@Dao
interface PageDao {

    @Query("SELECT id, scan_id, next FROM page WHERE scan_id = :scanId")
    fun getByScanId(scanId: Int): Observable<List<PageEntity>>

    @Query("SELECT id, scan_id, next FROM page WHERE scan_id = :scanId and next is null")
    fun getLastPage(scanId: Int): Single<PageEntity>

    @Query("SELECT * FROM page WHERE id = :pageId")
    fun getPageImage(pageId: Int): Single<PageEntity>

    @Insert
    fun insert(page: PageEntity): Single<Long>

    @Query("UPDATE page SET next = :next WHERE id = :pageId")
    fun updateNext(pageId: Int, next: Int?): Single<Int>

    @Query("DELETE FROM page WHERE id = :pageId")
    fun delete(pageId: Int): Single<Int>

    @Query("DELETE FROM page WHERE scan_id = :scanId")
    fun deleteByScanId(scanId: Int): Single<Int>
}