package io.github.proshian.neuralswipetyping.swipePointFeaturesExtraction

import io.github.proshian.neuralswipetyping.keyboardGrid.KeyboardKey
import io.github.proshian.neuralswipetyping.tokenizers.KeyboardTokenizer
import io.github.proshian.neuralswipetyping.keyboardGrid.KeyboardGrid
import org.pytorch.executorch.EValue
import org.pytorch.executorch.Tensor
import kotlin.math.pow


fun getKeyCenter(key: KeyboardKey): Pair<Float, Float> {
    return Pair(key.hitbox.x + key.hitbox.w / 2f, key.hitbox.y + key.hitbox.h / 2f)
}

fun getSquaredDistance(coords1: Pair<Float, Float>, coords2: Pair<Float, Float>): Float {
    return (coords1.first - coords2.first).pow(2) + (coords1.second - coords2.second).pow(2)
}

fun getNearestKeyTokenWithoutMap(i: Int, j: Int, keys: List<KeyboardKey>): Int {
    val keyIdToDistance = keys.map { getSquaredDistance(getKeyCenter(it), Pair(i.toFloat(), j.toFloat())) }.toFloatArray()
    return keyIdToDistance.withIndex().minByOrNull { it.value }!!.index
}


class NearestKeysGetter(grid: KeyboardGrid, allowedLabels: Set<String>? = null) : FeatureExtractor {
    private val nkl: Array<IntArray>
    private val tokenizer = KeyboardTokenizer()
    val resolvedAllowedLabels = allowedLabels ?: tokenizer.tokenToId.keys
    private val allowedKeys: List<KeyboardKey.CharacterKey> =
        grid.keys.filterIsInstance<KeyboardKey.CharacterKey>()
            .filter { it.label in resolvedAllowedLabels }
    private val keyboardWidth: Int = grid.width
    private val keyboardHeight: Int = grid.height

    init {
        nkl = createNearestKeysArray(allowedKeys)
    }

    private fun createNearestKeysArray(allowedKeys: List<KeyboardKey.CharacterKey>): Array<IntArray> {
        val labeledKeyboardArr = Array(keyboardWidth) { IntArray(keyboardHeight) { -1 } }

        for (key in allowedKeys) {
            val token = tokenizer.tokenToId[key.label]!!
            val hitbox = key.hitbox

            val xLeft = hitbox.x
            val xRight = hitbox.x + hitbox.w
            val yTop = hitbox.y
            val yBottom = hitbox.y + hitbox.h

            for (i in xLeft until xRight) {
                for (j in yTop until yBottom) {
                    labeledKeyboardArr[i][j] = token
                }
            }
        }

        for (i in 0 until keyboardWidth) {
            for (j in 0 until keyboardHeight) {
                if (labeledKeyboardArr[i][j] != -1) continue
                labeledKeyboardArr[i][j] = getNearestKeyTokenWithoutMap(i, j, allowedKeys)
            }
        }

        return labeledKeyboardArr
    }

    override fun invoke(x: IntArray, y: IntArray, t: IntArray): Array<EValue> {
        val nearestKeys = IntArray(x.size)
        for (i in nearestKeys.indices) {
            val xi = x[i]
            val yi = y[i]
            nearestKeys[i] = if (xi in 0 until keyboardWidth && yi in 0 until keyboardHeight) {
                nkl[xi][yi]
            } else {
                getNearestKeyTokenWithoutMap(xi, yi, allowedKeys)
            }
        }
        val tensor = Tensor.fromBlob(nearestKeys, longArrayOf(nearestKeys.size.toLong(), 1))
        return arrayOf(EValue.from(tensor))
    }
}