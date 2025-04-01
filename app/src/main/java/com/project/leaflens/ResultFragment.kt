package com.project.leaflens

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ResultFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private var imageUri: Uri? = null
    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        imageView = view.findViewById(R.id.imageViewResult)
        resultText = view.findViewById(R.id.textResult)

        // Retrieve the image URI from arguments
        arguments?.getString("imagePath")?.let {
            imageUri = Uri.parse(it)
            Log.d("ResultFragment", "Received image URI: $imageUri")
            processImage()
        }

        return view
    }



    private fun processImage() {
        try {
            imageUri?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                imageView.setImageBitmap(bitmap)
                Log.d("ResultFragment", "Original image size: ${bitmap.width} x ${bitmap.height}")

                // Load the TFLite Model
                interpreter = Interpreter(loadModelFile())

                // Model requires 300x300 input
                val inputWidth = 300
                val inputHeight = 300
                Log.d("ResultFragment", "Using model input dimensions: $inputWidth x $inputHeight")

                // Resize the image to 300x300
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
                Log.d("ResultFragment", "Resized image size: ${resizedBitmap.width} x ${resizedBitmap.height}")

                // Create input buffer - using uint8 (0-255)
                val byteBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * 3)
                byteBuffer.order(ByteOrder.nativeOrder())

                val intValues = IntArray(inputWidth * inputHeight)
                resizedBitmap.getPixels(intValues, 0, inputWidth, 0, 0, inputWidth, inputHeight)

                // Convert image to uint8 format
                for (i in intValues.indices) {
                    val pixel = intValues[i]
                    byteBuffer.put(((pixel shr 16) and 0xFF).toByte()) // Red
                    byteBuffer.put(((pixel shr 8) and 0xFF).toByte())  // Green
                    byteBuffer.put((pixel and 0xFF).toByte())         // Blue
                }

                byteBuffer.rewind()
                Log.d("ResultFragment", "Input buffer size: ${byteBuffer.capacity()} bytes")

                // Run inference
                val outputArray = Array(1) { ByteArray(16) } // Model outputs uint8, not float
                interpreter.run(byteBuffer, outputArray)

                // Convert output to probabilities
                val probabilities = outputArray[0].map { it.toInt() and 0xFF }.map { it / 255.0f }

                // Display the prediction
                val result = getTopPrediction(probabilities.toFloatArray())
                Log.d("ResultFragment", "Prediction result: $result")
                resultText.text = result
            }
        } catch (e: Exception) {
            Log.e("ResultFragment", "Error processing image", e)
            resultText.text = "Error processing image: ${e.message}"
            e.printStackTrace()
        }
    }


    private fun loadModelFile(): MappedByteBuffer {
        return try {
            val assetFileDescriptor = requireContext().assets.openFd("1.tflite")
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val mappedByteBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.declaredLength
            )
            fileInputStream.close()
            assetFileDescriptor.close()
            Log.d("ResultFragment", "Model loaded successfully")
            mappedByteBuffer
        } catch (e: IOException) {
            Log.e("ResultFragment", "Model file not found!", e)
            throw RuntimeException("Failed to load model file.")
        }
    }

    private fun getTopPrediction(output: FloatArray): String {
        val labels = listOf(
            "Tomato Healthy", "Tomato Septoria Leaf Spot", "Tomato Bacterial Spot",
            "Tomato Blight", "Cabbage Healthy", "Tomato Spider Mite",
            "Tomato Leaf Mold", "Tomato Yellow Leaf Curl Virus", "Soy Frogeye Leaf Spot",
            "Soy Downy Mildew", "Maize Ravi Corn Rust", "Maize Healthy",
            "Maize Grey Leaf Spot", "Maize Lethal Necrosis", "Soy Healthy", "Cabbage Black Rot"
        )

        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return if (maxIndex != -1) {
            "${labels[maxIndex]} (Confidence: ${"%.2f".format(output[maxIndex] * 100)}%)"
        } else {
            "Prediction failed"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::interpreter.isInitialized) {
            Log.d("ResultFragment", "Closing TensorFlow Lite Interpreter")
            interpreter.close()
        }
    }
}
