package com.xpl0t.scany.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.room.Room
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Document
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.entities.DocumentEntity
import com.xpl0t.scany.repository.entities.PageEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.combineLatest
import io.reactivex.rxjava3.core.Observable.just
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context,
    private val pageImageStore: PageImageStore
) : Repository {

    private val db = Room.databaseBuilder(context, AppDatabase::class.java, "scany-db")
        .build()

    init {
        val sharedPref = context.getSharedPreferences(REPOSITORY_PREF_KEY, MODE_PRIVATE)
        val dbInitialized = sharedPref.getBoolean(DB_INITIALIZED, false)

        if (!dbInitialized) {
            initDb().subscribeOn(Schedulers.computation()).subscribe(
                { Log.i(TAG, "Database successfully initialized") },
                { Log.e(TAG, "Database initialization failed", it) },
                {
                    val prefEditor = sharedPref.edit()
                    prefEditor.putBoolean(DB_INITIALIZED, true)
                    prefEditor.apply()
                }
            )
        }
    }

    private fun initDb(): Observable<List<Long>> {
        val documents = arrayOf(
            DocumentEntity(1, context.resources.getString(R.string.doc_name_1))
        )
        val pages = arrayOf(
            PageEntity(1, 1, 0),
            PageEntity(2, 1, 1)
        )

        return db.documentDao().insert(*documents)
            .concatMap { db.pageDao().insert(*pages) }
            .doOnSuccess {
                for (pageId in it) {
                    val pageImg = getAsset("$pageId.png")
                    pageImageStore.create(pageId.toInt(), pageImg)
                }
            }
            .toObservable()
    }

    private fun getAsset(fileName: String): ByteArray {
        val asset = context.assets.open(fileName)
        val buffer = ByteArray(1024 * 100)
        val output = ByteArrayOutputStream()

        var bytesRead: Int
        while (asset.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }

        return output.toByteArray()
    }

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
                getDocument(it.first().toInt()).take(1)
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
                pageImageStore.create(it.first().toInt(), page.image!!)
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
        const val TAG = "RepositoryImpl"
        const val REPOSITORY_PREF_KEY = "INIT_DB"
        const val DB_INITIALIZED = "INIT_DB"
    }
}