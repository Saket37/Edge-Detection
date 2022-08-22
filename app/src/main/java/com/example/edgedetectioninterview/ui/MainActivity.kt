package com.example.edgedetectioninterview.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.edgedetectioninterview.databinding.ActivityMainBinding
import com.example.edgedetectioninterview.utils.BaseActivity
import com.example.edgedetectioninterview.utils.BitmapHelper
import com.example.edgedetectioninterview.utils.Status
import com.example.edgedetectioninterview.utils.UrlHelper.toBitmap
import com.example.edgedetectioninterview.utils.getImageUri
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var bitmapHelper: BitmapHelper

    private val mainViewModel: MainViewModel by viewModels()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var processedImageUri: String = ""
    private var originalImageUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OpenCVLoader.initDebug()
        loadImageFromGallery()
        takeImageFromCamera()
        loadUrlImage()
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun loadUrlImage() {

        binding.uploadUrl.setOnClickListener {
            val urlAsAString = binding.url.text.toString().trim()
            if (urlAsAString.isNotEmpty() || urlAsAString.isNotBlank()) {
                val url = URL(urlAsAString)

                val result: Deferred<Bitmap?> = GlobalScope.async {
                    url.toBitmap()
                }
                GlobalScope.launch(Dispatchers.Main) {
                    // show bitmap on image view when available
                    result.await()?.let { it1 ->
                        detectEdges(it1)
                    }
                }
            } else {
                Toast.makeText(this, "Url Field is Empty", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadImageFromGallery() {
        val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                try {
                    detectEdges(bitmapHelper.readBitmapFromPath(this, it))

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            })
        binding.gallery.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickPhoto.launch("image/*")
            } else {
                Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun detectEdges(bitmap: Bitmap) {
        val rgba = Mat()

        Utils.bitmapToMat(bitmap, rgba)
        val edges = Mat(rgba.size(), CvType.CV_8UC1)
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4)
        Imgproc.Canny(edges, edges, 80.0, 100.0)

        bitmapHelper.showBitmap(this, bitmap, binding.uploadedImage)
        val uploadedUri = getImageUri(this, bitmap)
        Log.d("UPLOADED_URI", uploadedUri.toString())

        val resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(edges, resultBitmap)
        bitmapHelper.showBitmap(
            this,
            resultBitmap,
            binding.processedImage
        )
        val processedUri = getImageUri(this, resultBitmap)
        Log.d("PROCESSED_URI", processedUri.toString())


        if (uploadedUri != null && processedUri != null) {
            val uriToUpload = arrayListOf(uploadedUri, processedUri)
            lifecycleScope.launch {
                mainViewModel.uploadImagesToFirebase(uriToUpload).collectLatest {
                    when (it.status) {
                        Status.SUCCESS -> {
                            originalImageUri = it.data?.data?.get(0).toString()
                            processedImageUri = it.data?.data?.get(1).toString()
                            Log.d("UPLOADED_DOWNLOAD_URI", originalImageUri.toString())
                            Log.d("PROCESSED_DOWNLOAD_URI", processedImageUri.toString())
                        }
                        Status.ERROR -> {

                        }
                        Status.LOADING -> {

                        }
                    }
                }

                mainViewModel.uploadToFirestore(originalImageUri, processedImageUri).collectLatest {
                    when (it.status) {
                        Status.SUCCESS -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Images uploaded to database",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        Status.ERROR -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Error uploading to Database",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Status.LOADING -> {
                            Toast.makeText(
                                this@MainActivity,
                                "uploading to Database",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }

    }


    private fun takeImageFromCamera() {
        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            try {
                if (it != null) {
                    detectEdges(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to capture Image.", Toast.LENGTH_SHORT).show()
            }
        }
        binding.takeImage.setOnClickListener {
            takePhoto.launch()
        }
    }

}

