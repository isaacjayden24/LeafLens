package com.project.leaflens.utils.upload

import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PreprocessImageObject {

     fun preprocessImage(bitmap: Bitmap, imageSize: Int): ByteBuffer {

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
    }
}