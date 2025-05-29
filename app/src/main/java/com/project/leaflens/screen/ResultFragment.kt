package com.project.leaflens.screen

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.project.leaflens.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel





class ResultFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var solutionBtn: Button
    private lateinit var descriptionTextView:TextView
    private lateinit var regionBtn: Button

    private var imageUri: Uri? = null
    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        imageView = view.findViewById(R.id.imageViewResult)
        resultText = view.findViewById(R.id.textResult)
        solutionBtn = view.findViewById(R.id.solutionBtn)
        descriptionTextView=view.findViewById(R.id.descriptionTextView)
        regionBtn=view.findViewById(R.id.regionBtn)




        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

       // Enable the back button
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

       // Handle the navigation icon click
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }







        // Retrieve the image URI from arguments
        arguments?.getString("imagePath")?.let {
            imageUri = Uri.parse(it)
            Log.d("ResultFragment", "Received image URI: $imageUri")
            processImage()
        }

        solutionBtn.setOnClickListener {
            // Get the predicted disease name from the resultText TextView
            val diseaseName = resultText.text.toString()
            Log.d("ResultFragment", "Disease name: $diseaseName")
            val action = ResultFragmentDirections.actionResultFragmentToSolutionFragment(diseaseName)
            findNavController().navigate(action)
        }

        regionBtn.setOnClickListener {

            imageUri?.let { uri ->
                val action = ResultFragmentDirections.actionResultFragmentToRegionFragment(uri.toString())
                findNavController().navigate(action)
            }
           // findNavController().navigate(R.id.action_resultFragment_to_regionFragment)
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

                // Model requires 224x224 input
                val inputWidth = 224
                val inputHeight = 224
                Log.d("ResultFragment", "Using model input dimensions: $inputWidth x $inputHeight")

                // Resize the image to 224x224
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
                Log.d("ResultFragment", "Resized image size: ${resizedBitmap.width} x ${resizedBitmap.height}")

                // Create input buffer - using float (0-1) values
                val byteBuffer = ByteBuffer.allocateDirect(inputWidth * inputHeight * 3 * 4)
                byteBuffer.order(ByteOrder.nativeOrder())

                val intValues = IntArray(inputWidth * inputHeight)
                resizedBitmap.getPixels(intValues, 0, inputWidth, 0, 0, inputWidth, inputHeight)

                // Normalize pixel values to [0, 1] range
                for (i in intValues.indices) {
                    val pixel = intValues[i]
                    byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f) // Red
                    byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)  // Green
                    byteBuffer.putFloat((pixel and 0xFF) / 255.0f)         // Blue
                }

                byteBuffer.rewind()
                Log.d("ResultFragment", "Input buffer size: ${byteBuffer.capacity()} bytes")

                // Run inference with the corrected output size
                val outputArray = Array(1) { FloatArray(4) }  // Model's output shape is [1, 4]
                interpreter.run(byteBuffer, outputArray)

                // Use the output directly as probabilities (no further scaling)
                val probabilities = outputArray[0]

                // Display the prediction
                val result = getTopPrediction(probabilities)
                Log.d("ResultFragment", "Prediction result: $result")

               // Display formatted result
                val confidence = probabilities.maxOrNull()?.times(100) ?: 0.0f
                resultText.text = "$result (Confidence: ${"%.2f".format(confidence)}%)"

               // Check the result in the `when` statement
                when (result) {

                    "Gray Leaf Spot"->descriptionTextView.text="Gray Leaf Spot is a fungal disease caused primarily by *Cercospora* species, affecting turfgrasses like ryegrass and corn. It appears as small, gray to brown lesions on leaves, often with a yellow halo, leading to leaf blight and reduced photosynthesis. Favorable conditions include warm, humid environments, and it spreads through wind, rain, or contaminated equipment.\n"
                    "Healthy"->descriptionTextView.text="Healthy plant!"
                    "Blight" -> descriptionTextView.text = "Blight is a plant disease caused by fungi, bacteria, or environmental factors, leading to rapid browning, wilting, and death of affected plant tissues. It commonly affects leaves, stems, and flowers, spreading quickly under warm, humid conditions, and can result in significant crop loss."

                    "Common Rust" -> descriptionTextView.text = "Common rust is caused by fungal pathogens from the Puccinia genus. It appears as reddish-brown pustules on leaves, which can coalesce and cause leaf yellowing and premature death. Warm, moist conditions promote its spread, impacting the plant’s ability to photosynthesize effectively."
                    else -> descriptionTextView.text = "Unknown disease detected."
                }





            }
        } catch (e: Exception) {
            Log.e("ResultFragment", "Error processing image", e)
            resultText.text = "Error processing image: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        return try {

            val assetFileDescriptor = requireContext().assets.openFd("mobilenetv2_v1_44_0.996.tflite")
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
            "Blight", "Common Rust", "Gray Leaf Spot", "Healthy"
        )

        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return if (maxIndex != -1) {
            labels[maxIndex]  // Return only the label
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
