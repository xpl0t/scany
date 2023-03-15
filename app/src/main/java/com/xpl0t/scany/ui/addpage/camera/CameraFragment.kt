package com.xpl0t.scany.ui.addpage.camera

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.os.Bundle
import android.util.Log
import android.util.Size
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
import com.xpl0t.scany.util.Stopwatch
import com.xpl0t.scany.views.DocumentOutline
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import android.graphics.Point
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON


@AndroidEntryPoint
class CameraFragment : BaseFragment(R.layout.camera_fragment), ImageAnalysis.Analyzer {

    private val args: CameraFragmentArgs by navArgs()

    @Inject()
    lateinit var service: CameraService

    private lateinit var cameraPreview: PreviewView
    private lateinit var documentOutline: DocumentOutline
    private lateinit var takePhotoBtn: FloatingActionButton
    private lateinit var switchFlashBtn: FloatingActionButton

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraExecutor: ExecutorService? = null

    private val blendKernel: Mat = getBlendKernel()

    /**
     * Minimum width proportion of the total width to get detected as document.
     */
    private val minDocWidthProportion = 0.2
    /**
     * Minimum width proportion of the total height to get detected as document.
     */
    private val minDocHeightProportion = 0.2

    private var lastTimeDocDetected: Long? = null

    /**
     * The time the outline is displayed after the document could not be detected anymore.
     * This prevents outline flickering but can look weird if the outline is kept too long
     * in other occasions.
     */
    private val outlineTimeout: Long = 50

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
        documentOutline = requireView().findViewById(R.id.doc_outline)
        cameraPreview = requireView().findViewById(R.id.cam_preview)
        takePhotoBtn = requireView().findViewById(R.id.take_photo)
        takePhotoBtn.setOnClickListener {
            Log.d(TAG, "Take photo btn clicked")
            takePhotoBtn.isEnabled = false
            takePhoto()
        }
        switchFlashBtn = requireView().findViewById(R.id.switch_flash)
        switchFlashBtn.setOnClickListener {
            Log.d(TAG, "Switch flash button clicked")
            toggleFlashMode()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setBufferFormat(ImageFormat.YUV_420_888)
                .build()

            setFlashMode(getFlashMode())

            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis!!.setAnalyzer(Executors.newSingleThreadExecutor(), this)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                val camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )

                val hasFlash = camera?.cameraInfo?.hasFlashUnit() ?: false
                switchFlashBtn.visibility = if (hasFlash) View.VISIBLE else View.GONE
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            cameraExecutor!!,
            object : ImageCapture.OnImageCapturedCallback() {
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
        val stopwatch = Stopwatch()
        Log.i(TAG, "Process image")

        stopwatch.start()
        val mat = image.toMat()
        image.close()
        var processingMat = mat.clone()
        Log.d(TAG, "Conversion from yuv to rgb bitmap took ${stopwatch.stop()} milliseconds")

        stopwatch.start()
        processingMat = processingMat
            .scale(640.0)
            .grayscale()
            .blend(blendKernel)
            .canny()

        val docContour = processingMat.getLargestQuadrangle(
            (processingMat.width() * minDocWidthProportion).toInt(),
            (processingMat.height() * minDocHeightProportion).toInt()
        )
        if (docContour == null) {
            runOnUiThread {
                takePhotoBtn.isEnabled = true
            }

            Log.e(TAG, "No quadrangle contour could be found")
            Snackbar.make(requireView(), R.string.no_doc_found, Snackbar.LENGTH_SHORT).show()
            return
        }

        val scalingFactor = mat.height().toDouble() / processingMat.height().toDouble()
        val quadrangleEdges = docContour.toList().map { it.multiply(scalingFactor) }

        val warpedMat = mat.perspectiveTransform(quadrangleEdges)

        Log.d(TAG, "Image processing took ${stopwatch.stop()} milliseconds")

        runOnUiThread {
            showImproveFragment(warpedMat)
        }
    }

    override fun analyze(image: ImageProxy) {
        var mat = image.toGrayscaleMat()
        image.close()

        mat = mat
            .blend(blendKernel)
            .canny()

        val docContour = mat.getLargestQuadrangle(
            (mat.width() * minDocWidthProportion).toInt(),
            (mat.height() * minDocHeightProportion).toInt()
        )

        if (docContour == null) {
            if (lastTimeDocDetected != null && System.currentTimeMillis() - lastTimeDocDetected!! > outlineTimeout) {
                this.documentOutline.clear()
            }

            return
        }

        // Inverting width/height, because input frame is rotated
        val curWidth = mat.height().toFloat()
        val curHeight = mat.width().toFloat()

        val targetXYRatio = documentOutline.height.toFloat() / documentOutline.width.toFloat()
        val curXYRatio = curHeight / curWidth

        val scalingFactor = if (targetXYRatio > curXYRatio)
            documentOutline.height.toFloat() / curHeight // Cur is wider
        else
            documentOutline.width.toFloat() / curWidth // Cur is higher

        val offX = if (targetXYRatio > curXYRatio) (curWidth * scalingFactor - documentOutline.width.toFloat()) / 2
            else 0f
        val offY = if (targetXYRatio < curXYRatio) (curHeight * scalingFactor - documentOutline.height.toFloat()) / 2
            else 0f

        val edges = docContour.toList().map {
            // Rotating coordinates
            val x = mat.height() - it.y
            val y = it.x

            Point((x * scalingFactor - offX).toInt(), (y * scalingFactor - offY).toInt())
        }

        documentOutline.setOutline(edges)
        lastTimeDocDetected = System.currentTimeMillis()
    }

    private fun getFlashMode(): Int {
        val pref = requireContext().getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE)
        return pref.getInt(FLASH_MODE, FLASH_MODE_OFF)
    }

    private fun setFlashMode(flashMode: Int) {
        val pref = requireContext().getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE)
        val prefEditor = pref.edit()
        prefEditor.putInt(FLASH_MODE, flashMode)
        prefEditor.apply()

        imageCapture!!.flashMode = flashMode

        val switchFlashBtnIcon = when (flashMode) {
            FLASH_MODE_ON -> R.drawable.flash_on
            FLASH_MODE_OFF -> R.drawable.flash_off
            else -> R.drawable.flash_off
        }
        switchFlashBtn.setImageResource(switchFlashBtnIcon)
    }

    private fun toggleFlashMode() {
        val curFlashMode = getFlashMode()
        val newFlashMode = if (curFlashMode == FLASH_MODE_ON) FLASH_MODE_OFF
            else FLASH_MODE_ON

        setFlashMode(newFlashMode)
    }

    /**
     * Set fragment result bundle and finish fragment.
     */
    private fun showImproveFragment(image: Mat) {
        service.page = image
        val action = CameraFragmentDirections
            .actionCameraFragmentToImproveFragment(args.documentId)
        findNavController().navigate(action)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getBlendKernel(size: Int = 8): Mat {
        val kernelArr = ByteArray(size * size)
        for (i in 0 until size * size) kernelArr[i] = 1

        val kernel = Mat(5, 5, CvType.CV_8U)
        kernel.put(0, 0, kernelArr)
        return kernel
    }

    companion object {
        const val TAG = "CameraFragment"
        const val PREFERENCES_KEY = "CAMERA_FRAGMENT"
        const val FLASH_MODE = "FLASH_MODE"

        private val REQUIRED_PERMISSIONS = listOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}