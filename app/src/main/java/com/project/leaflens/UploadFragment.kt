package com.project.leaflens

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


/**
 * A simple [Fragment] subclass.
 * Use the [UploadFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class UploadFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var uploadButton: Button
    private lateinit var classifyButton: Button
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        imageView = view.findViewById(R.id.imageView)
        uploadButton = view.findViewById(R.id.uploadButton)
        classifyButton = view.findViewById(R.id.classifyButton)

        // Button to open image picker
        uploadButton.setOnClickListener {
            pickImage()
        }

        // Button to navigate to ResultFragment with selected image
        classifyButton.setOnClickListener {
            selectedImageUri?.let {
                val bundle = Bundle().apply {
                    putString("imagePath", it.toString()) // Pass URI as string
                }
                findNavController().navigate(R.id.action_uploadFragment_to_resultFragment, bundle)
            } ?: run {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            }
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
            Toast.makeText(requireContext(), "Image selection canceled", Toast.LENGTH_SHORT).show()
        }
    }
}