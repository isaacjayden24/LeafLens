package com.project.leaflens.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.project.leaflens.utils.ImagePreprocessor
import com.project.leaflens.utils.TFLiteModelLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter

class ResultViewModel(application: Application): AndroidViewModel(application) {

    private lateinit var interpreter: Interpreter


    // prediction for resultFragment
    private val _predictionResult = MutableLiveData<Pair<String, Float>>() // label + confidence
    val predictionResult: LiveData<Pair<String, Float>> get() = _predictionResult

    // description for resultFragment
    private val _description = MutableLiveData<String>()
    val description: LiveData<String> get() = _description



    fun processImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = getApplication<Application>().contentResolver
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                val byteBuffer = ImagePreprocessor.convertBitmapToByteBuffer(resizedBitmap)

                interpreter = Interpreter(TFLiteModelLoader.loadModelFile(getApplication()))
                val outputArray = Array(1) { FloatArray(4) }
                interpreter.run(byteBuffer, outputArray)

                val resultLabel = getTopPrediction(outputArray[0])
                val confidence = outputArray[0].maxOrNull()?.times(100f) ?: 0f

                _predictionResult.postValue(resultLabel to confidence)
                _description.postValue(getDescription(resultLabel))

            } catch (e: Exception) {
                Log.e("ResultViewModel", "Image processing failed", e)
                _predictionResult.postValue("Error" to 0f)
                _description.postValue("An error occurred while processing the image.")
            }
        }


    }

    private fun getTopPrediction(probabilities: FloatArray): String {
        val labels = listOf("Blight", "Common Rust", "Gray Leaf Spot", "Healthy")
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        return labels.getOrElse(maxIndex) { "Unknown" }
    }

    private fun getDescription(label: String): String {
        return when (label) {
            "Gray Leaf Spot" -> "Gray Leaf Spot is a fungal disease caused primarily by *Cercospora* species, affecting turfgrasses like ryegrass and corn. It appears as small, gray to brown lesions on leaves, often with a yellow halo, leading to leaf blight and reduced photosynthesis. Favorable conditions include warm, humid environments, and it spreads through wind, rain, or contaminated equipment.\n"
            "Healthy" -> "Healthy plant!"
            "Blight" -> "Blight is a plant disease caused by fungi, bacteria, or environmental factors, leading to rapid browning, wilting, and death of affected plant tissues. It commonly affects leaves, stems, and flowers, spreading quickly under warm, humid conditions, and can result in significant crop loss."
            "Common Rust" -> "Common rust is caused by fungal pathogens from the Puccinia genus. It appears as reddish-brown pustules on leaves, which can coalesce and cause leaf yellowing and premature death. Warm, moist conditions promote its spread, impacting the plant’s ability to photosynthesize effectively."
            else -> "Unknown disease detected."
        }
    }





}