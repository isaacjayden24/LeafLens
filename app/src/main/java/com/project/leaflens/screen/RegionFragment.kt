package com.project.leaflens.screen

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.project.leaflens.R


/**
 * A simple [Fragment] subclass.
 * Use the [RegionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegionFragment : Fragment() {
    private lateinit var originalImageView: ImageView
    private lateinit var processedImageView: ImageView

    private var imageUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_region, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        originalImageView = view.findViewById(R.id.originalImage)
        processedImageView = view.findViewById(R.id.processedImage)

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
            Log.d("RegionFragment", "Received image URI: $imageUri")

            // Load and display the original image
            originalImageView.setImageURI(imageUri)

            // Load your image from URI for processing
            val originalBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)

            // Process the image to highlight diseased regions
            val processedBitmap = highlightDiseasedRegions(originalBitmap)
            processedImageView.setImageBitmap(processedBitmap)
        }








    }






    private fun highlightDiseasedRegions(bitmap: Bitmap): Bitmap? {
        // a mutable copy of the original image
        val output = bitmap.config?.let { bitmap.copy(it, true) }

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixel = bitmap.getPixel(x, y)

                // Extract RGB components
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                // Improved color threshold for disease detection
                if (red > 120 && green < 100 && blue < 100) {
                    // Use a bright and more visible color
                    val highlightColor = Color.argb(200, 255, 0, 0) // Semi-transparent red

                    // Make the highlighted region thicker
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            val nx = x + dx
                            val ny = y + dy
                            // bounds checking before coloring
                            if (nx in 0 until bitmap.width && ny in 0 until bitmap.height) {
                                output?.setPixel(nx, ny, highlightColor)
                            }
                        }
                    }
                }
            }
        }
        return output
    }


}



/* private fun highlightDiseasedRegions(bitmap: Bitmap): Bitmap? {
       val output = bitmap.config?.let { bitmap.copy(it, true) }

       for (x in 0 until bitmap.width) {
           for (y in 0 until bitmap.height) {
               val pixel = bitmap.getPixel(x, y)

               //  Identify yellowish/brownish regions
               val red = Color.red(pixel)
               val green = Color.green(pixel)
               val blue = Color.blue(pixel)

               //  color threshold for disease detection
               if (red > 100 && green < 100 && blue < 100) {
                   output?.setPixel(x, y, Color.RED) // Highlight with red color
               }
           }
       }
       return output
   }*/