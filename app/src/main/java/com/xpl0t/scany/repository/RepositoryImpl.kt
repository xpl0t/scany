package com.xpl0t.scany.repository

import android.content.Context
import androidx.room.Room
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Document
import com.xpl0t.scany.repository.entities.PageEntity
import com.xpl0t.scany.repository.entities.DocumentEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.combineLatest
import io.reactivex.rxjava3.core.Observable.just
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    val pageImageStore: PageImageStore
) : Repository {

    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "scany-db").build()

    override fun getDocuments(): Observable<List<Document>> {
        val pairObs = combineLatest(
            db.documentDao().getAll(),
            db.pageDao().getAll()
        ) { document, pages ->
            Pair(document, pages)
        }

        return pairObs
            .map { pair ->
                return@map pair.first.map { document ->
                    val pages = pair.second
                        .filter { it.documentId == document.id }
                        .sortedBy { it.order }
                        .map { Page(it.id, null, it.order) }

                    return@map Document(document.id, document.name, pages)
                }
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun getDocumentCount(): Observable<Int> {
        return db.documentDao().getCount()
            .toObservable()
            .subscribeOn(Schedulers.computation())
    }

    override fun getDocument(id: Int): Observable<Document> {
        val documentPagesPairObs = combineLatest(
            db.documentDao().get(id),
            db.pageDao().getByDocumentId(id)
        ) { document, pages ->
            Pair(document, pages)
        }

        return documentPagesPairObs.map {
            val document = it.first
            val pages = it.second
                .sortedBy { it.order }
                .map { Page(it.id, null, it.order) }

            Document(document.id, document.name, pages)
        }.subscribeOn(Schedulers.computation())
    }

    override fun getPageImage(pageId: Int): Single<ByteArray> {
        return Single.fromCallable { pageImageStore.read(pageId) }
            .subscribeOn(Schedulers.computation())
    }

    override fun addDocument(document: Document): Observable<Document> {
        val entity = DocumentEntity(0, document.name)

        return db.documentDao().insert(entity).toObservable()
            .concatMap {
                getDocument(it.toInt()).take(1)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun updateDocument(document: Document): Observable<Document> {
        val entity = DocumentEntity(document.id, document.name)

        return db.documentDao().update(entity).toObservable()
            .concatMap {
                getDocument(document.id).take(1)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun addPage(documentId: Int, page: Page): Observable<Document> {
        return db.pageDao().getLastPage(documentId)
            .concatMap {
                val order = if (it.isEmpty()) 0
                    else it.first().order + 100

                val entity = PageEntity(page.id, documentId, order)
                db.pageDao().insert(entity)
            }
            .toObservable()
            .doOnNext {
                pageImageStore.create(it.toInt(), page.image!!)
            }
            .concatMap {
                getDocument(documentId).take(1)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun removePage(id: Int): Observable<Int> {
        return db.pageDao().delete(id)
            .doAfterSuccess {
                pageImageStore.delete(id)
            }
            .toObservable()
            .subscribeOn(Schedulers.computation())
    }

    override fun removeDocument(id: Int): Observable<Int> {
        return db.pageDao().getByDocumentId(id)
            .concatMap { pages ->
                db.documentDao().delete(id)
                    .map { pages }
                    .toObservable()
            }
            .doAfterNext {
                for (page in it)
                    pageImageStore.delete(page.id)
            }
            .map { 0 }
            .subscribeOn(Schedulers.computation())
    }

    override fun reorderPages(documentId: Int, pages: List<Page>): Observable<List<Page>> {
        val updatedPages = pages.indices.map {
            pages[it].copy(order = it * 100)
        }
        val updateObs = updatedPages
            .map { db.pageDao().updateOrder(it.id, it.order).toObservable() }

        if (updateObs.isEmpty()) {
            return just(pages)
        }

        return combineLatest(updateObs) { 0 }
            .map { updatedPages.toList() }
            .subscribeOn(Schedulers.computation())
    }

    companion object {
        val TAG = "RepositoryImpl"
    }
}