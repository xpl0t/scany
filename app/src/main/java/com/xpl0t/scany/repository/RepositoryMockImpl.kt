package com.xpl0t.scany.repository

import com.xpl0t.scany.models.Scan
import com.xpl0t.scany.models.ScanImage
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RepositoryMockImpl @Inject constructor() : Repository {

    private val scanSubject: BehaviorSubject<List<Scan>> = BehaviorSubject.createDefault(getMockScans())

    private fun shouldFail(): Boolean {
        return (Random.nextInt() % 5 == 0) and false
    }

    private fun getMockScans(): List<Scan> {
        return listOf(
            Scan(1, "Test Scan :)", images = listOf()),
            Scan(2, "Test 123", images = listOf()),
            Scan(3, "Testy testy", images = listOf()),
            Scan(4, "Testo testo", images = listOf())
        )
    }

    override fun getScans(): Observable<List<Scan>> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        return scanSubject
    }

    override fun getScan(id: Int): Observable<Scan> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        return scanSubject.concatMap {
            val scan = scanSubject.value!!.find { it.id == id }
                ?: return@concatMap Observable.error<Scan>(Error("No scan with id $id found!"))

            Observable.just(scan)
        }
    }

    override fun addScan(scan: Scan): Observable<Scan> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = scanSubject.value!!.toMutableList()
        val maxId = newList
            .map { it.id }
            .maxOrNull() ?: 0

        val newScan = scan.copy(id = maxId + 1)
        newList.add(newScan)
        scanSubject.onNext(newList)

        return Observable.just(newScan)
    }

    override fun updateScan(scan: Scan): Observable<Scan> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = scanSubject.value!!.toMutableList()
        val idx = newList.indexOfFirst { it.id == scan.id }
        if (idx == -1)
            return Observable.error(Error("No scan with id ${scan.id} found!"))

        newList.removeAt(idx)
        newList.add(idx, scan)
        scanSubject.onNext(newList)

        return Observable.just(scan)
    }

    override fun addScanImg(scanId: Int, scanImg: ScanImage): Observable<Scan> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = scanSubject.value!!.toMutableList()
        val idx = newList.indexOfFirst { it.id == scanId }
        if (idx == -1)
            return Observable.error(Error("No scan with id $scanId found!"))

        val scan = newList[idx]
        val scanImgId = if (scan.images.isEmpty()) 1 else scan.images.maxOf { it.id } + 1
        val newScanImgList = scan.images.toMutableList()
        newScanImgList.add(scanImg.copy(id = scanImgId))
        val newScan = scan.copy(images = newScanImgList)

        newList.removeAt(idx)
        newList.add(idx, newScan)
        scanSubject.onNext(newList)

        return Observable.just(newScan)
    }

    override fun removeScan(id: Int): Observable<Int> {
        if (shouldFail()) return Observable.error(Error("Database offline"))

        val newList = scanSubject.value!!.toMutableList()
        newList.removeAll { it.id == id }
        scanSubject.onNext(newList)

        return Observable.just(0)
    }

}