package com.xpl0t.scany.repository

import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Document
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RepositoryMockImpl @Inject constructor() : Repository {

    private val documentSubject: BehaviorSubject<List<Document>> =
        BehaviorSubject.createDefault(getMockDocuments())

    private fun shouldFail(): Boolean {
        return (Random.nextInt() % 5 == 0) and false
    }

    private fun getMockDocuments(): List<Document> {
        return listOf(
            Document(1, "Test Document :)", pages = listOf()),
            Document(2, "Test 123", pages = listOf()),
            Document(3, "Testy testy", pages = listOf()),
            Document(4, "Testo testo", pages = listOf())
        )
    }

    override fun getDocuments(): Observable<List<Document>> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        return documentSubject
    }

    override fun getDocumentCount(): Observable<Int> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        return Observable.just(8)
    }

    override fun getDocument(id: Int): Observable<Document> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        return documentSubject.concatMap {
            val document = documentSubject.value!!.find { it.id == id }
                ?: return@concatMap Observable.error<Document>(Error("No document with id $id found!"))

            Observable.just(document)
        }
    }

    override fun getPageImage(pageId: Int): Single<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun addDocument(document: Document): Observable<Document> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = documentSubject.value!!.toMutableList()
        val maxId = newList
            .map { it.id }
            .maxOrNull() ?: 0

        val newDocument = document.copy(id = maxId + 1)
        newList.add(newDocument)
        documentSubject.onNext(newList)

        return Observable.just(newDocument)
    }

    override fun updateDocument(document: Document): Observable<Document> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = documentSubject.value!!.toMutableList()
        val idx = newList.indexOfFirst { it.id == document.id }
        if (idx == -1)
            return Observable.error(Error("No document with id ${document.id} found!"))

        newList.removeAt(idx)
        newList.add(idx, document)
        documentSubject.onNext(newList)

        return Observable.just(document)
    }

    override fun addPage(documentId: Int, documentImg: Page): Observable<Document> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = documentSubject.value!!.toMutableList()
        val idx = newList.indexOfFirst { it.id == documentId }
        if (idx == -1)
            return Observable.error(Error("No document with id $documentId found!"))

        val document = newList[idx]
        val documentImgId = if (document.pages.isEmpty()) 1 else document.pages.maxOf { it.id } + 1
        val newDocumentImgList = document.pages.toMutableList()
        newDocumentImgList.add(documentImg.copy(id = documentImgId))
        val newDocument = document.copy(pages = newDocumentImgList)

        newList.removeAt(idx)
        newList.add(idx, newDocument)
        documentSubject.onNext(newList)

        return Observable.just(newDocument)
    }

    override fun removePage(id: Int): Observable<Int> {
        throw NotImplementedError()
    }

    override fun removeDocument(id: Int): Observable<Int> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = documentSubject.value!!.toMutableList()
        newList.removeAll { it.id == id }
        documentSubject.onNext(newList)

        return Observable.just(0)
    }

    override fun reorderPages(documentId: Int, pages: List<Page>): Observable<List<Page>> {
        TODO("Not yet implemented")
    }
}