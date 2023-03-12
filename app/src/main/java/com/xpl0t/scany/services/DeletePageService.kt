package com.xpl0t.scany.services

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xpl0t.scany.R
import com.xpl0t.scany.repository.Repository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class DeletePageService @Inject() constructor(
    private val repo: Repository
) {

    fun showDeletePageDialog(context: Context, pageId: Int): Observable<Int> {
        val dlgObs = Observable.create<Int> {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete_page)
                .setMessage(R.string.delete_page_question)
                .setNegativeButton(R.string.cancel_btn) { _, _ ->
                    it.onComplete()
                }
                .setPositiveButton(R.string.delete_btn) { _, _ ->
                    it.onNext(0)
                }
                .show()
        }

        return dlgObs.concatMap {
            repo.removePage(pageId)
        }
    }

}