package com.tstine.myapplication

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.tstine.myapplication.databinding.ActivityMainBinding
import com.tstine.myapplication.ui.theme.PencilSketch
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayInputStream


class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    companion object {
        init {
            if (!OpenCVLoader.initDebug()) {
                println("OpenCV initialization failed.")
            }
        }
    }

    val PICK_IMAGE = 1

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
            )
        }

        binding.btn.setOnClickListener {

            pickImage()
        }


    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
    }

    private fun processImage(bitmap: Bitmap) {
        // Chuyển đổi Bitmap thành Mat
        val rgbaImg = Mat()
        Utils.bitmapToMat(bitmap, rgbaImg)

        // Chuyển ảnh sang ảnh xám
        val grayImg = Mat()
        Imgproc.cvtColor(rgbaImg, grayImg, Imgproc.COLOR_RGBA2GRAY)

        // Tạo ảnh biên cạnh
        val edgeImg = Mat()
        Imgproc.Canny(grayImg, edgeImg, 255.0, 0.0)

        // Đảo ngược ảnh biên cạnh
        Core.bitwise_not(edgeImg, edgeImg)

        // Chuyển đổi sang ảnh RGBA
        Imgproc.cvtColor(edgeImg, rgbaImg, Imgproc.COLOR_GRAY2RGBA)

        val whiteMask = Mat()
        val lowerb = Scalar(255.0, 255.0, 255.0, 255.0)
        val upperb = Scalar(255.0, 255.0, 255.0, 255.0)
        Core.inRange(rgbaImg, lowerb, upperb, whiteMask)
        rgbaImg.setTo(Scalar(0.0, 0.0, 0.0, 0.0), whiteMask)

        val processedBitmap = Bitmap.createBitmap(whiteMask.cols(), whiteMask.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgbaImg, processedBitmap)


        binding.processedImageView.setImageBitmap(processedBitmap)
    }

    // Initialize Python
    private val python: Python by lazy {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        Python.getInstance()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            val uri = data?.data

            if (uri != null) {

                Log.d("421841894002189402", "onActivityResult 0:")
                val imageBitmap = uriToBitmap(uri)
                binding.imageView.setImageBitmap(imageBitmap)

//                processImage(imageBitmap)
                val path = PathUtil.getPathFromUri(this, uri)
                if (path != null) {
                    Log.d("421841894002189402", "onActivityResult: $path")
                    callPencilSketch(path)
//                    binding.processedImageView.setImageBitmap(callPencilSketch(path))
                }
            }
        }
    }

    private fun callPencilSketch(imagePath: String): Bitmap? {
        // Import the Python module
        val pencilSketchModule: PyObject = python.getModule("PencilSketch")

        // Call the PencilSketch class
//        val pencilSketchClass: PyObject = pencilSketchModule.callAttr("PencilSketch")

        // Load the image file
        val frame: ByteArray = loadByteArrayFromFile(imagePath)

        // Call the PencilSketch class with the image
        val resultByteArray: ByteArray = pencilSketchModule.callAttr("__call__", frame).toJava(ByteArray::class.java)

        // Convert the result ByteArray to a Bitmap
        return if (resultByteArray.isNotEmpty()) {
            BitmapFactory.decodeStream(ByteArrayInputStream(resultByteArray))
        } else {
            null
        }
    }

    private fun loadByteArrayFromFile(filePath: String): ByteArray {
        val contentResolver: ContentResolver = applicationContext.contentResolver
        val uri: Uri = Uri.parse(filePath)

        // Open an input stream from the content resolver
        contentResolver.openInputStream(uri)?.use { inputStream ->
            // Read the file into a ByteArray
            return inputStream.readBytes()
        }

        // Return an empty ByteArray if there's an issue
        return byteArrayOf()
    }

    // Add other necessary code for your MainActivity



}
