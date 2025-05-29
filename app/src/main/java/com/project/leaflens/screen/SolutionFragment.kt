package com.project.leaflens.screen

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.project.leaflens.R
import kotlinx.coroutines.launch
import com.project.leaflens.BuildConfig

/**
 * A simple [Fragment] subclass.
 * Use the [SolutionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SolutionFragment : Fragment() {
    private lateinit var treatmentTextView: TextView
    private lateinit var preventionTextView: TextView
    private lateinit var causeTextView: TextView






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_solution, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        causeTextView = view.findViewById(R.id.causeTextView)
        preventionTextView = view.findViewById(R.id.preventionTextView)
        treatmentTextView = view.findViewById(R.id.treatmentTextview)



        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle the navigation icon click
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }




        // Check for internet connection before making API call
        if (isInternetAvailable()) {
            lifecycleScope.launch {
                try {

                    arguments?.let {
                        val diseaseName = arguments?.getString("diseaseName") ?: "Unknown"
                        Log.d("SolutionFragment", "Disease Name: $diseaseName")

                        getDiseaseSolution(diseaseName)
                    }
                } catch (e: Exception) {
                    Log.e("SolutionFragment", "Error fetching disease solution: ${e.message}", e)
                    showSnackbar("An error occurred while fetching data.")
                }
            }
        } else {
            showSnackbar("No internet connection. Please check your network settings.")
        }




    }











    //function to initialize the sdk
    suspend fun getDiseaseSolution(diseaseName:String){
        val generativeModel=GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY


        )

        //causes prompt
        val prompt="Explain the causes  for the disease (in 2 statements)  $diseaseName"
        val response= generativeModel.generateContent(prompt)
        val generatedText=response.text
        causeTextView.text= generatedText

        //prevention prompt
        val prompt2="Explain the prevention  for the disease (in 2 statements) $diseaseName"
        val response2= generativeModel.generateContent(prompt2)
        val generatedText2=response2.text
        preventionTextView.text= generatedText2

        //treatment prompt
       val prompt3="Explain the treatment  for the disease (in 2 statements) $diseaseName"
        val response3= generativeModel.generateContent(prompt3)
        val generatedText3=response3.text
        treatmentTextView.text= generatedText3




    }



    //function to display no internet connection snackbar
    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

    //function to check for internet connection
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }






}