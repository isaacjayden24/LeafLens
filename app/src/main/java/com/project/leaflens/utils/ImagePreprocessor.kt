package com.project.leaflens.utils

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImagePreprocessor {
    fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        for (pixel in intValues) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            byteBuffer.putFloat((pixel and 0xFF) / 255f)
        }

        byteBuffer.rewind()
        return byteBuffer
    }
}