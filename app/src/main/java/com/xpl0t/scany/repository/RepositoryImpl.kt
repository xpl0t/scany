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
import io.reactivex.rxjava3.core.Observable.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
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
            !pages.any { p.id == it.next }
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

                next = nextPage.next
                loopRuns++
            }

            if (pages.count() != sortedPages.count()) {
                Log.w(TAG, "Order corrupt, resetting order")
                sortedPages = pages.toMutableList()
            }

            Scan(
                scan.id, scan.name,
                sortedPages.map { Page(it.id, it.image, it.next) }
            )
        }.subscribeOn(Schedulers.computation())
    }

    override fun getPageImage(pageId: Int): Single<ByteArray> {
        return db.pageDao().getPageImage(pageId)
            .map { it.image!! }
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
        val entity = PageEntity(page.id, scanId, page.image, null)

        val lastPageObs = db.pageDao().getLastPage(scanId)
            .toObservable()
            .map { Optional(it) }
            .onErrorResumeNext { just(Optional.empty()) } // Might be null if there were no pages

        return lastPageObs
            .concatMap { lastPage ->
                db.pageDao().insert(entity).toObservable().map {
                    Pair(it, lastPage)
                }
            }
            .concatMap {
                val newPageId = it.first
                val previousLastPage = it.second

                if (!previousLastPage.isEmpty)
                    db.pageDao().updateNext(previousLastPage.value.id, newPageId.toInt())
                        .toObservable()
                else
                    just(0)
            }
            .concatMap {
                getScan(scanId).take(1)
            }
            .subscribeOn(Schedulers.computation())
    }

    override fun removeScan(id: Int): Observable<Int> {
        return db.scanDao().delete(id)
            .toObservable()
            .subscribeOn(Schedulers.computation())
    }

    override fun reorderPages(scanId: Int, pages: List<Page>): Observable<List<Page>> {
        val updatedPages = mutableListOf<Page>()
        val updateObs = mutableListOf<Observable<Int>>()

        for (i in 0 until pages.count()) {
            val next = if (i + 1 < pages.count()) pages[i + 1].id else null

            updatedPages.add(pages[i].copy(next = next))
            if (next != pages[i].next) {
                updateObs.add(
                    db.pageDao().updateNext(pages[i].id, next).toObservable()
                )
            }
        }

        if (updateObs.count() == 0) {
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