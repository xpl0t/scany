package com.xpl0t.scany.ui.addpage.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.*
import com.xpl0t.scany.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class CameraFragment : BaseFragment(R.layout.camera_fragment) {

    private val args: CameraFragmentArgs by navArgs()

    @Inject()
    lateinit var service: CameraService

    private lateinit var cameraPreview: PreviewView
    private lateinit var takePhotoBtn: FloatingActionButton

    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            // TODO(): Use activity result api.
            requestPermissions(REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != REQUEST_CODE_PERMISSIONS) return

        if (allPermissionsGranted()) {
            Log.i(TAG, "Camera permission granted")
            startCamera()
        } else {
            Log.e(TAG, "Camera permission not granted")
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

            imageCapture = ImageCapture.Builder()
                .setBufferFormat(ImageFormat.YUV_420_888)
                .build()

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

        imageCapture.takePicture(cameraExecutor!!, object : ImageCapture.OnImageCapturedCallback() {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                Snackbar.make(requireView(), R.string.error_msg, Snackbar.LENGTH_SHORT).show()
                runOnUiThread {
                    findNavController().popBackStack()
                }
            }

            override fun onCaptureSuccess(image: ImageProxy) {
                Log.i(TAG, "Photo capture successful")
                processImage(image)
            }
        }
        )
    }

    private fun processImage(image: ImageProxy) {
        Log.i(TAG, "Process image")

        var mat: Mat?
        var dur = measureTimeMillis {
            mat = image.toMat()
        }
        Log.d(TAG, "Conversion from yuv to rgb bitmap took $dur milliseconds")

        dur = measureTimeMillis {
            mat!!.apply {
                scale(500.0)
                grayscale()
                blur()
                canny()
            }

            val docContour = mat!!.getLargestQuadrilateral()
            if (docContour == null) {
                Log.e(TAG, "No quadrilateral contour could be found")
                Snackbar.make(requireView(), R.string.no_doc_found, Snackbar.LENGTH_SHORT).show()
                return
            }

            //val cont = MatOfPoint()
            // docContour.convertTo(cont, CV_32S)

            for (p in docContour.toArray()) {
                Imgproc.drawMarker(mat!!, p, Scalar(240.0, 2.0, 5.0))
            }
            // Imgproc.drawContours(mat!!, mutableListOf(cont), -1, Scalar(0.0, 255.0, 0.0), 2)
        }
        Log.d(TAG, "Image processing took $dur milliseconds")

        runOnUiThread {
            showImproveFragment(mat!!)
        }
    }

    /**
     * Set fragment result bundle and finish fragment.
     */
    private fun showImproveFragment(image: Mat) {
        service.page = image
        val action = CameraFragmentDirections
            .actionCameraFragmentToImproveFragment(args.scanId)
        findNavController().navigate(action)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val TAG = "CameraFragment"
        private val REQUIRED_PERMISSIONS = listOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}