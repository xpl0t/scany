package com.xpl0t.scany.repository

import com.xpl0t.scany.models.Scan
import io.reactivex.rxjava3.core.Observable

interface Repository {

    /**
     * Get an observable emitting an iterable of scans.
     */
    fun getScans(): Observable<List<Scan>>

    /**
     * Get an observable emitting the scan with the specified id if found and null otherwise.
     */
    fun getScan(id: Int): Observable<Scan>

    /**
     * Adds a scan and returns an observable emitting the new scan once added.
     */
    fun addScan(scan: Scan): Observable<Scan>

    /**
     * Updates a scan and returns an observable emitting the updated scan once added.
     */
    fun updateScan(scan: Scan): Observable<Scan>

    /**
     * Removes a scan and returns an observable emitting once the removal succeeded.
     * The emitted number is meant to be ignored and has no significance.
     */
    fun removeScan(id: Int): Observable<Int>

}