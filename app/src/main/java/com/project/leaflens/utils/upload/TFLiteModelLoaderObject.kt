package com.project.leaflens.utils.upload

import android.content.Context
import org.tensorflow.lite.Interpreter

object TFLiteModelLoaderObject {
    fun loadModel(context: Context, modelPath: String): Interpreter {
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
    }
}