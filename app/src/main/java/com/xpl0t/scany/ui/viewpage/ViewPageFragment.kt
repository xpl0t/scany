package com.xpl0t.scany.ui.viewpage

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.runOnUiThread
import com.xpl0t.scany.extensions.toJpg
import com.xpl0t.scany.filter.Filter
import com.xpl0t.scany.models.Page
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.scan.ScanFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.opencv.core.Mat
import javax.inject.Inject

@AndroidEntryPoint
class ViewPageFragment : BaseFragment(R.layout.view_page) {

    private val args: ViewPageFragmentArgs by navArgs()

    @Inject()
    lateinit var repo: Repository

    private var disposable: Disposable? = null

    // private lateinit var mat: Mat

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
                    true
                }
                else -> false
            }
        }
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