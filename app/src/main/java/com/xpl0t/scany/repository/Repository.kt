package com.xpl0t.scany.repository

import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Scan
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface Repository {

    /**
     * Get an observable emitting an iterable of scans.
     */
    fun getScans(): Observable<List<Scan>>

    /**
     * Get an observable emitting the amount of scans.
     */
    fun getScanCount(): Observable<Int>

    /**
     * Get an observable emitting the scan with the specified id if found.
     */
    fun getScan(id: Int): Observable<Scan>

    /**
     * Get an observable emitting the page image as ByteArray.
     */
    fun getPageImage(pageId: Int): Single<ByteArray>

    /**
     * Adds a scan and returns an observable emitting the new scan once added.
     */
    fun addScan(scan: Scan): Observable<Scan>

    /**
     * Updates a scan and returns an observable emitting the updated scan once added.
     */
    fun updateScan(scan: Scan): Observable<Scan>

    /**
     * Adds a scan and returns an observable emitting the new scan once added.
     */
    fun addPage(scanId: Int, page: Page): Observable<Scan>

    /**
     * Removes a page and returns an observable emitting once the removal succeeded.
     * The emitted number is meant to be ignored and has no significance.
     */
    fun removePage(id: Int): Observable<Int>

    /**
     * Removes a scan and returns an observable emitting once the removal succeeded.
     * The emitted number is meant to be ignored and has no significance.
     */
    fun removeScan(id: Int): Observable<Int>

    /**
     * Reorders pages based on the list provided.
     */
    fun reorderPages(scanId: Int, pages: List<Page>): Observable<List<Page>>

}