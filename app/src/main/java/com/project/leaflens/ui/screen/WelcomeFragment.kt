package com.project.leaflens.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.leaflens.R



class WelcomeFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnStartCamera = view.findViewById<ImageView>(R.id.btnStartCamera)
        btnStartCamera.setOnClickListener {
           // findNavController().navigate(R.id.action_welcomeFragment_to_cameraFragment)
            findNavController().navigate(R.id.action_welcomeFragment_to_uploadFragment)
        }
    }


}