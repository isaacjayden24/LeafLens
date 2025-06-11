package com.project.leaflens.utils.upload

import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

object runInferenceObject {
     fun runInference(model: Interpreter, inputBuffer: ByteBuffer): Float {
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1), DataType.FLOAT32)
        model.run(inputBuffer, outputBuffer.buffer.rewind())
        return outputBuffer.floatArray[0]
    }
}