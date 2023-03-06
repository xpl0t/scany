package com.xpl0t.scany.ui.scanlist.scannamegenerator

import android.content.Context
import com.xpl0t.scany.R
import com.xpl0t.scany.repository.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SequentialDocumentNameGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: Repository
) : ScanNameGenerator {

    /**
     * Generate new document name.
     */
    override fun generate(): Observable<String> {
        return repo.getScanCount()
            .map { context.resources.getString(R.string.new_document_name, it + 1) }
    }

}