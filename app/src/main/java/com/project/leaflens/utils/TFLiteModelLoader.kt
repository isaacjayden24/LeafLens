package com.project.leaflens.utils

import android.app.Application
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object TFLiteModelLoader {
    fun loadModelFile(application: Application): MappedByteBuffer {
        val fileDescriptor = application.assets.openFd("mobilenetv2_v1_44_0.996.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }
}