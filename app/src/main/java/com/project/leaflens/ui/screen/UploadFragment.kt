package com.project.leaflens.ui.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.project.leaflens.R
import com.project.leaflens.utils.upload.UriBitmapConvertObject.uriToBitmap
import com.project.leaflens.viewmodel.ResultViewModel


class UploadFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var classifyButton: Button
    private val resultViewModel: ResultViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        imageView = view.findViewById(R.id.imageView)
        uploadButton = view.findViewById(R.id.uploadButton)
        classifyButton = view.findViewById(R.id.classifyButton)

        setUpToolBar(view)


        // Button to open image picker
        uploadButton.setOnClickListener {
            pickImage()
        }

        // Button to classify the selected image
        classifyButton.setOnClickListener {
            selectImage()
        }

        return view
    }

    // Open Gallery to select an image
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePicker.launch(intent)
    }

    // Handle image selection result
    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                imageView.setImageURI(uri) // Display selected image
            }
        } else {
            showToast("Image selection canceled")
        }
    }

    // Convert Uri to Bitmap
  /*  private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }*/

    // Validate the image using TFLite model
   /* private fun validateMaizeImage(bitmap: Bitmap): Boolean {
        val inputSize = 224
        val modelPath = "maize_detector_V1_23_0.990.tflite"

        return try {
            // Load the TFLite model
            val model = TFLiteModelLoaderObject.loadModel(requireContext(), modelPath)

            // Prepare the input buffer
            val inputBuffer = PreprocessImageObject.preprocessImage(bitmap, inputSize)

            // Run inference and get prediction
            val prediction = runInferenceObject.runInference(model, inputBuffer)

            // Close the model
            model.close()

            // Return true if prediction is valid
            prediction >= 0.5f
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Error during classification: ${e.message}")
            false
        }
    }*/

    // Load the TFLite model from assets as a MappedByteBuffer
   /* private fun loadModel(context: Context, modelPath: String): Interpreter {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = assetFileDescriptor.createInputStream()
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        // Load the model as a MappedByteBuffer
        val modelByteBuffer = fileChannel.map(
            java.nio.channels.FileChannel.MapMode.READ_ONLY,
            startOffset,
            declaredLength
        )
        return Interpreter(modelByteBuffer)
    }*/

   /* private fun preprocessImage(bitmap: Bitmap, imageSize: Int): ByteBuffer {

        val convertedBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }


        val scaledBitmap = Bitmap.createScaledBitmap(convertedBitmap, imageSize, imageSize, true)

        //  ByteBuffer to store the image data
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3) // 4 bytes per float, 3 channels (RGB)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Iterate through the pixels and add them to the ByteBuffer
        for (y in 0 until imageSize) {
            for (x in 0 until imageSize) {
                val pixel = scaledBitmap.getPixel(x, y)

                // Extract RGB values and normalize them to [0, 1]
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                // Put the normalized values into the buffer
                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }
        }

        return byteBuffer
    }*/




    // Run the inference and obtain the prediction
    /*private fun runInference(model: Interpreter, inputBuffer: ByteBuffer): Float {
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1), DataType.FLOAT32)
        model.run(inputBuffer, outputBuffer.buffer.rewind())
        return outputBuffer.floatArray[0]
    }*/


    //set up of tool bar
    private fun setUpToolBar(view: View) {
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }


    //function to validate image selection
    fun selectImage(){
        selectedImageUri?.let { uri ->
            val bitmap = uriToBitmap(uri,requireContext())
            bitmap?.let {
                if (resultViewModel.validateMaizeImage(requireContext(),it)) {
                    val bundle = Bundle().apply {
                        putString("imagePath", uri.toString())
                    }
                    findNavController().navigate(R.id.action_uploadFragment_to_resultFragment, bundle)
                } else {
                    showToast("The image is not valid.")
                }
            }
        } ?: run {
            showToast("Please select an image.")
        }
    }

    // Display a toast message
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}




