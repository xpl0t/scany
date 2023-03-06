package com.xpl0t.scany.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.xpl0t.scany.repository.entities.ScanEntity
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

    @Query("SELECT count(*) FROM scan")
    fun getCount(): Single<Int>

    @Insert
    fun insert(scan: ScanEntity): Single<Long>

    @Update
    fun update(scan: ScanEntity): Single<Int>

    @Query("DELETE FROM scan WHERE id = :scanId")
    fun delete(scanId: Int): Single<Int>
}