package com.tstine.myapplication.ui.theme

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class PencilSketch(blurSigma: Double, sharpenValue: Double) {
    private val blurSigma: Double = blurSigma
    private val sharpenValue: Double = sharpenValue

    fun applyPencilSketch(inputPath: String): Mat {
        // Read the input image
        val frame = Imgcodecs.imread(inputPath)

        // Apply pencil sketch effect
        val pencilSketchResult = pencilSketch(frame)

        // Convert Mat to Bitmap if needed
        // val bitmapResult = Bitmap.createBitmap(pencilSketchResult.cols(), pencilSketchResult.rows(), Bitmap.Config.ARGB_8888)
        // Utils.matToBitmap(pencilSketchResult, bitmapResult)

        return pencilSketchResult
    }

    private fun pencilSketch(inputFrame: Mat): Mat {
        // Convert the image to grayscale
        val gray = Mat()
        Imgproc.cvtColor(inputFrame, gray, Imgproc.COLOR_BGR2GRAY)

        // Apply Gaussian blur
        val blurred = Mat()
        Imgproc.GaussianBlur(gray, blurred, org.opencv.core.Size(0.0, 0.0), blurSigma)

        // Subtract the blurred image from the original to get the pencil sketch effect
        val pencilSketch = Mat()
        Core.subtract(gray, blurred, pencilSketch)

        // Apply sharpening
        val sharpeningKernel = Mat(3, 3, CvType.CV_32F, Scalar.all(-1.0))
        sharpeningKernel.put(1, 1, sharpenValue)
        Imgproc.filter2D(pencilSketch, pencilSketch, -1, sharpeningKernel)

        // Convert back to BGR color space
        val result = Mat()
        Imgproc.cvtColor(pencilSketch, result, Imgproc.COLOR_GRAY2BGR)

        return result
    }
}