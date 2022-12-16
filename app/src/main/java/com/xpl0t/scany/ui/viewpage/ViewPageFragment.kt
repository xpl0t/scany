package com.xpl0t.scany.ui.viewpage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.services.DeletePageService
import com.xpl0t.scany.services.ShareService
import com.xpl0t.scany.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@AndroidEntryPoint
class ViewPageFragment : BaseFragment(R.layout.view_page) {

    private val args: ViewPageFragmentArgs by navArgs()

    @Inject()
    lateinit var repo: Repository

    @Inject()
    lateinit var shareSv: ShareService

    @Inject()
    lateinit var deletePageSv: DeletePageService

    private var disposable: Disposable? = null
    private var actionDisposable: Disposable? = null

    private var image: ByteArray? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bitmapPreview: PhotoView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    override fun onResume() {
        super.onResume()

        repo.getPageImage(args.pageId).subscribeBy(
            {
                Log.e(TAG, "Could not load page", it)
                Snackbar.make(view!!, R.string.load_page_err, Snackbar.LENGTH_LONG)
                finish()
            },
            {
                image = it
                runOnUiThread {
                    setDocPreview(it)
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    private fun initViews() {
        toolbar = requireView().findViewById(R.id.toolbar)
        bitmapPreview = requireView().findViewById(R.id.bitmapPreview)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.share -> {
                    shareSv.shareImage(context!!, image ?: return@setOnMenuItemClickListener true)
                    true
                }
                R.id.delete -> {
                    initDeletePage()
                    true
                }
                else -> false
            }
        }
    }

    private fun initDeletePage() {
        if (actionDisposable?.isDisposed == false)
            return

        Log.i(TAG, "Delete page ${args.pageId}")

        deletePageSv.showDeletePageDialog(context!!, args.pageId).subscribe(
            {
                Log.d(TAG, "Delete page succeeded")
                runOnUiThread {
                    finish()
                }
            },
            {
                Log.e(TAG, "Delete page failed", it)
                Snackbar.make(requireView(), R.string.delete_page_failed, Snackbar.LENGTH_LONG)
                    .show()
            }
        )
    }

    private fun setDocPreview(byteArray: ByteArray) {
        Glide.with(requireView())
            .load(byteArray)
            .into(bitmapPreview)
    }

    companion object {
        const val TAG = "ViewPageFragment"
    }
}
