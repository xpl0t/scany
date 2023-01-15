package com.xpl0t.scany.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.repository.entities.PageEntity
import com.xpl0t.scany.repository.entities.ScanEntity
import com.xpl0t.scany.util.Optional
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

    override fun getScans(): Observable<List<Scan>> {
        return db.scanDao().getAll().map {
            it.map {
                Scan(it.id, it.name)
            }
        }.subscribeOn(Schedulers.computation())
    }

    private fun findFirstPage(pages: List<PageEntity>): PageEntity? {
        return pages.find { p ->
            !pages.any { p.id == it.order }
        }
    }

    override fun getScan(id: Int): Observable<Scan> {
        val scanPagesPairObs = combineLatest(
            db.scanDao().get(id),
            db.pageDao().getByScanId(id)
        ) { scan, pages ->
            Pair(scan, pages)
        }

        return scanPagesPairObs.map {
            val scan = it.first
            val pages = it.second
            var sortedPages = mutableListOf<PageEntity>()
            var next = findFirstPage(pages)?.id
            var loopRuns = 0

            while (next != null) {
                if (loopRuns >= pages.count()) {
                    Log.w(TAG, "Detected infinite loop while ordering pages")
                    sortedPages = pages.toMutableList()
                    break
                }

                val nextPage = pages.find { it.id == next } ?: break
                sortedPages.add(nextPage)

                next = nextPage.order
                loopRuns++
            }

            if (pages.count() != sortedPages.count()) {
                Log.w(TAG, "Order corrupt, resetting order")
                sortedPages = pages.toMutableList()
            }

            Scan(
                scan.id, scan.name,
                sortedPages.map { Page(it.id, null, it.order) }
            )
        }.subscribeOn(Schedulers.computation())
    }

    override fun getPageImage(pageId: Int): Single<ByteArray> {
        return Single.fromCallable { pageImageStore.read(pageId) }
            .subscribeOn(Schedulers.computation())
    }

    override fun addScan(scan: Scan): Observable<Scan> {
        val entity = ScanEntity(0, scan.name)

        return db.scanDao().insert(entity).toObservable()
            .concatMap {
                getScan(it.toInt()).take(1)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun updateScan(scan: Scan): Observable<Scan> {
        val entity = ScanEntity(scan.id, scan.name)

        return db.scanDao().update(entity).toObservable()
            .concatMap {
                getScan(scan.id).take(1)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun addPage(scanId: Int, page: Page): Observable<Scan> {
        return db.pageDao().getLastPage(scanId)
            .concatMap {
                val order = if (it.isEmpty()) 0
                    else it.first().order + 100

                val entity = PageEntity(page.id, scanId, order)
                db.pageDao().insert(entity)
            }
            .toObservable()
            .doOnNext {
                pageImageStore.create(it.toInt(), page.image!!)
            }
            .concatMap {
                getScan(scanId).take(1)
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

    override fun removeScan(id: Int): Observable<Int> {
        return db.pageDao().getByScanId(id)
            .concatMap { pages ->
                db.scanDao().delete(id)
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

    override fun reorderPages(scanId: Int, pages: List<Page>): Observable<List<Page>> {
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