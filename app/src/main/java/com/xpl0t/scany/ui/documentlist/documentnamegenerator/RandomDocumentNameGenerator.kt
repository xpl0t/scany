package com.xpl0t.scany.ui.documentlist.documentnamegenerator

import android.content.Context
import com.xpl0t.scany.R
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class RandomDocumentNameGenerator @Inject constructor(
    @ApplicationContext val context: Context
) : DocumentNameGenerator {

    /**
     * Generate random document name.
     */
    override fun generate(): Observable<String> {
        val documentNames = context.resources.getStringArray(R.array.random_document_names)
        return Observable.just(documentNames.random())
    }

}