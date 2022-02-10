package com.xpl0t.scany.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.finish
import com.xpl0t.scany.extensions.showFragment
import com.xpl0t.scany.extensions.toBitmap
import com.xpl0t.scany.ui.common.BaseFragment
import com.xpl0t.scany.ui.crop.CropFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : BaseFragment(R.layout.camera_fragment) {

    private lateinit var cameraPreview: PreviewView
    private lateinit var takePhotoBtn: FloatingActionButton

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != REQUEST_CODE_PERMISSIONS) return

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Snackbar.make(requireView(), R.string.missing_permission_err, Snackbar.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    private fun initViews() {
        cameraPreview = requireView().findViewById(R.id.camPreview)
        takePhotoBtn = requireView().findViewById(R.id.takePhoto)
        takePhotoBtn.setOnClickListener {
            Log.d(TAG, "Take photo btn clicked")
            takePhoto()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    Log.i(TAG, "Photo capture successful")
                    val bitmap = image.toBitmap()
                    showCropFragment(bitmap)
                }
            }
        )
    }

    /**
     * Set fragment result bundle and finish fragment.
     */
    private fun showCropFragment(image: Bitmap) {
        val bundle = Bundle().apply {
            putParcelable(SOURCE_BITMAP, image)
        }

        parentFragmentManager.showFragment(CropFragment(), true, bundle)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "CameraFragment"
        const val SOURCE_BITMAP = "SOURCE_BITMAP"
        private val REQUIRED_PERMISSIONS = listOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}