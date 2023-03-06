package com.xpl0t.scany.repository

import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Document
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface Repository {

    /**
     * Get an observable emitting an iterable of documents.
     */
    fun getDocuments(): Observable<List<Document>>

    /**
     * Get an observable emitting the amount of documents.
     */
    fun getDocumentCount(): Observable<Int>

    /**
     * Get an observable emitting the document with the specified id if found.
     */
    fun getDocument(id: Int): Observable<Document>

    /**
     * Get an observable emitting the page image as ByteArray.
     */
    fun getPageImage(pageId: Int): Single<ByteArray>

    /**
     * Adds a document and returns an observable emitting the new document once added.
     */
    fun addDocument(document: Document): Observable<Document>

    /**
     * Updates a document and returns an observable emitting the updated document once added.
     */
    fun updateDocument(document: Document): Observable<Document>

    /**
     * Adds a document and returns an observable emitting the new document once added.
     */
    fun addPage(documentId: Int, page: Page): Observable<Document>

    /**
     * Removes a page and returns an observable emitting once the removal succeeded.
     * The emitted number is meant to be ignored and has no significance.
     */
    fun removePage(id: Int): Observable<Int>

    /**
     * Removes a document and returns an observable emitting once the removal succeeded.
     * The emitted number is meant to be ignored and has no significance.
     */
    fun removeDocument(id: Int): Observable<Int>

    /**
     * Reorders pages based on the list provided.
     */
    fun reorderPages(documentId: Int, pages: List<Page>): Observable<List<Page>>

}