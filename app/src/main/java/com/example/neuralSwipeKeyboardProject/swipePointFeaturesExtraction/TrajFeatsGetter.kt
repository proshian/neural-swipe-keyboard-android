package com.example.neuralSwipeKeyboardProject.swipePointFeaturesExtraction

import org.pytorch.executorch.Tensor
import android.util.Log
import org.pytorch.executorch.EValue

fun getDxDt(x: FloatArray, t: FloatArray): FloatArray {
    val dxDt = FloatArray(x.size){0f}
    for (i in 1 until x.size - 1) {
        dxDt[i] = (x[i + 1] - x[i - 1]) / (t[i + 1] - t[i - 1])
    }
    return dxDt
}

//val gridNameToWh = mapOf("default" to Pair(1080, 667))

class TrajFeatsGetter(
    private val includeTime: Boolean = false,
    private val includeVelocities: Boolean = true,
    private val includeAccelerations: Boolean = true,
    private val gridNameToWh: Map<String, Pair<Int, Int>> = mapOf("default" to Pair(1080, 667)),
    ) : FeatureExtractor {

    init {
        if (includeAccelerations && !includeVelocities) {
            throw IllegalArgumentException("Accelerations require velocities to be included.")
        }
    }

    fun getFeats(x: IntArray, y: IntArray, t: IntArray, gridName: String): Tensor {
        assert( (x.size == y.size) and (x.size == t.size) )

        val xFloat = x.map{ it.toFloat() }.toFloatArray()
        val yFloat = y.map{ it.toFloat() }.toFloatArray()
        val tFloat = t.map{ it.toFloat() }.toFloatArray()

        val trajectoryFeatsList = mutableListOf(xFloat, yFloat)

        if (includeTime) {
            trajectoryFeatsList.add(tFloat)
        }

        if (includeVelocities) {
            val dxDt = getDxDt(xFloat, tFloat)
            val dyDt = getDxDt(yFloat, tFloat)
            trajectoryFeatsList.add(dxDt)
            trajectoryFeatsList.add(dyDt)

            if (includeAccelerations) {
                val d2xDt2 = getDxDt(dxDt, tFloat)
                val d2yDt2 = getDxDt(dyDt, tFloat)
                trajectoryFeatsList.add(d2xDt2)
                trajectoryFeatsList.add(d2yDt2)
            }
        }

        val trajFeats = stackFloatArraysToTensor(trajectoryFeatsList)
        Log.i("myTag", "before normalization: ${trajFeats.dataAsFloatArray.joinToString(" ")}")

        val (width, height) = gridNameToWh[gridName] ?: throw IllegalArgumentException("Invalid grid name: $gridName")
        val trajFeatsNormed = normalizeTrajFeats(trajFeats, width.toFloat(), height.toFloat())

        return trajFeatsNormed
    }

    private fun stackFloatArraysToTensor(arrays: List<FloatArray>): Tensor {
        val featsNum = arrays.first().size
        val seqLen = arrays.size
        val resultData = FloatArray(featsNum * seqLen)

        for (i in 0 until featsNum) {
            for ((j, arr) in arrays.withIndex()) {
                resultData[i * seqLen + j] = arr[i]
            }
        }
        return Tensor.fromBlob(resultData, longArrayOf(featsNum.toLong(), 1, seqLen.toLong()))
    }


    private fun normalizeTrajFeats(tensor: Tensor, width: Float, height: Float): Tensor {
        val data = tensor.dataAsFloatArray
        val numRows = tensor.shape()[0].toInt()
        val numCols = tensor.shape()[2].toInt()

        for (i in 0 until numRows) {
            data[i * numCols] /= width       // Normalize X
            data[i * numCols + 1] /= height  // Normalize Y
        }

        return Tensor.fromBlob(data, tensor.shape())
    }

    override fun invoke(x: IntArray, y: IntArray, t: IntArray): Array<EValue> {
        val gridName = "default" // Use the default grid name for now
        val tensor = getFeats(x, y, t, gridName)
        return arrayOf(EValue.from(tensor))
    }
}
