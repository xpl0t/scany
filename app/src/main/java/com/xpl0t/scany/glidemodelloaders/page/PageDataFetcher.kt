package com.xpl0t.scany.glidemodelloaders.page

import androidx.annotation.NonNull
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.Repository
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.nio.ByteBuffer


class PageDataFetcher(
    private val model: Page,
    private val repo: Repository
) : DataFetcher<ByteBuffer> {

    private var loadDisposable: Disposable? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
        loadDisposable = repo.getPageImage(model.id).subscribeBy(
            onSuccess = {
                callback.onDataReady(ByteBuffer.wrap(it))
            },
            onError = {
                callback.onLoadFailed(Exception(it))
            }
        )
    }

    override fun cleanup() {
    }

    override fun cancel() {
        loadDisposable?.dispose()
    }

    @NonNull
    override fun getDataClass(): Class<ByteBuffer> {
        return ByteBuffer::class.java
    }

    @NonNull
    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}