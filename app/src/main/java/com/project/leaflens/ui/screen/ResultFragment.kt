package com.project.leaflens.ui.screen

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.project.leaflens.R
import com.project.leaflens.viewmodel.ResultViewModel


class ResultFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var solutionBtn: Button
    private lateinit var descriptionTextView:TextView
    private lateinit var regionBtn: Button

    private val resultViewModel:ResultViewModel by viewModels()

    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        imageView = view.findViewById(R.id.imageViewResult)
        resultText = view.findViewById(R.id.textResult)
        solutionBtn = view.findViewById(R.id.solutionBtn)
        descriptionTextView=view.findViewById(R.id.descriptionTextView)
        regionBtn=view.findViewById(R.id.regionBtn)




        setUpToolBar()
        setupObservers()











        arguments?.getString("imagePath")?.let {
            val uri = Uri.parse(it)
            imageUri=uri
            imageView.setImageURI(uri)
            Log.d("ResultFragment", "Received image URI: $uri")
            resultViewModel.processImage(uri)
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


    private fun setUpToolBar(){
        val toolbar = view?.findViewById<MaterialToolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle the navigation icon click
        toolbar?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        resultViewModel.predictionResult.observe(viewLifecycleOwner) { (label, confidence) ->
            resultText.text = "$label (Confidence: %.2f%%)".format(confidence)
        }

        resultViewModel.description.observe(viewLifecycleOwner) {
            descriptionTextView.text = it
        }
    }

}
