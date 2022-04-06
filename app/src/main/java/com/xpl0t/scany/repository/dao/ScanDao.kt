package com.xpl0t.scany.repository.dao

import androidx.room.*
import com.xpl0t.scany.repository.entities.ScanEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan")
    fun getAll(): Observable<List<ScanEntity>>

    @Query("SELECT * FROM scan WHERE id = :scanId")
    fun get(scanId: Int): Observable<ScanEntity>

    @Query("SELECT * FROM scan LIMIT 1 OFFSET :rowId")
    fun getByIdx(rowId: Long): Single<ScanEntity>

    @Insert
    fun insert(scan: ScanEntity): Single<Long>

    @Update
    fun update(scan: ScanEntity): Single<Int>

    @Query("DELETE FROM scan WHERE id = :scanId")
    fun delete(scanId: Int): Single<Int>
}