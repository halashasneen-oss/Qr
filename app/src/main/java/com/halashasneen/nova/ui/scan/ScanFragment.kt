package com.halashasneen.nova.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts
import com.halashasneen.nova.R
import com.halashasneen.nova.databinding.FragmentScanBinding
import com.halashasneen.nova.util.vibrateShort
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScanViewModel by activityViewModels()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: androidx.camera.core.Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var hasNavigated = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera() else showPermissionRequired()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnFlash.setOnClickListener { toggleFlash() }
        binding.btnGrantPermission.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (hasCameraPermission()) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun showPermissionRequired() {
        binding.permissionOverlay.visibility = View.VISIBLE
        binding.cameraPreview.visibility = View.GONE
    }

    private fun startCamera() {
        binding.permissionOverlay.visibility = View.GONE
        binding.cameraPreview.visibility = View.VISIBLE
        hasNavigated = false

        val providerFuture = ProcessCameraProvider.getInstance(requireContext())
        providerFuture.addListener({
            cameraProvider = providerFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRAnalyzer { value ->
                        onQrCodeDetected(value)
                    })
                }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    viewLifecycleOwner, selector, preview, analysis
                )
            } catch (e: Exception) {
                // فشل ربط الكاميرا (نادر جدًا)، نعرض حالة الإذن كحل بديل آمن
                showPermissionRequired()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun onQrCodeDetected(value: String) {
        if (hasNavigated) return
        hasNavigated = true
        requireContext().vibrateShort()
        viewModel.lastScannedValue = value

        val bundle = Bundle().apply {
            putString("rawContent", value)
            putBoolean("isGenerated", false)
        }
        findNavController().navigate(R.id.action_scan_to_result, bundle)
    }

    private fun toggleFlash() {
        val cam = camera ?: return
        if (!cam.cameraInfo.hasFlashUnit()) return
        viewModel.isFlashOn = !viewModel.isFlashOn
        cam.cameraControl.enableTorch(viewModel.isFlashOn)
        binding.btnFlash.setImageResource(
            if (viewModel.isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        _binding = null
    }
}
