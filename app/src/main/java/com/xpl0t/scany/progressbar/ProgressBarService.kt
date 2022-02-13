package com.xpl0t.scany.progressbar

import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressBarService @Inject constructor() {

    val loadingSubject = BehaviorSubject.createDefault(false)

    fun setLoading(loading: Boolean) {
        loadingSubject.onNext(loading)
    }

}