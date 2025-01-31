package com.example.executorch_neuroswipe_example_1.ngtFeaturesExtraction

import com.example.executorch_neuroswipe_example_1.tokenizers.KeyboardKeyTokenizer
import kotlin.math.pow

fun getKeyCenter(key: KeyboardKey): Pair<Float, Float> {
    return Pair(key.hitbox.x + key.hitbox.w / 2f, key.hitbox.y + key.hitbox.h / 2f)
}

fun getSquaredDistance(coords1: Pair<Float, Float>, coords2: Pair<Float, Float>): Float {
    return ((coords1.first - coords2.first).toDouble().pow(2.0)
    + (coords1.second - coords2.second).toDouble().pow(2.0)).toFloat()
}

fun getNearestKeyTokenWithoutMap(i: Int, j: Int, keys:  List<KeyboardKey>): Int {
    val keyIdToDistance = keys.map{getSquaredDistance(getKeyCenter(it), Pair(i.toFloat(), j.toFloat()))}.toFloatArray()

    return keyIdToDistance.withIndex().minByOrNull { it.value }!!.index
}

fun getNKL(): Array<IntArray>{
    val nearestKeyLabelCandidates = arrayOf(
        "а", "б", "в", "г", "д", "е", "ë", "ж", "з", "и", "й",
        "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф",
        "х", "ц", "ч", "ш", "щ", "ъ", "ы", "ь", "э", "ю", "я"
    )
    val grid = getDefaultGrid()
    val keyboardWidth = grid.width
    val keyboardHeight = grid.height
    val allowedKeys = grid.keys.filter {it.label in nearestKeyLabelCandidates}
    val tokenizer = KeyboardKeyTokenizer()
    val labeledKeyboardArr = Array(size = keyboardWidth) { IntArray(size = keyboardHeight) {-1} }


    for (key in allowedKeys) {
        val token = tokenizer.tokenToId[key.label]!!
        val hitbox = key.hitbox

        val xLeft = hitbox.x
        val xRight = hitbox.x + hitbox.w
        val yTop = hitbox.y
        val yBottom = yTop + hitbox.h

        for (i in xLeft..<xRight) {
            for (j in yTop..<yBottom) {
                labeledKeyboardArr[i][j] = token
            }
        }
    }

    for (i in 0..< keyboardWidth) {
        for (j in 0..<keyboardHeight) {
            if (labeledKeyboardArr[i][j] != -1) {
                continue
            }
            labeledKeyboardArr[i][j] = getNearestKeyTokenWithoutMap(i, j, allowedKeys)
        }
    }

    return labeledKeyboardArr
}


fun getNearestKeys(x: IntArray, y: IntArray, nkl: Array<IntArray>): IntArray {
    val nearestKeys = IntArray(size=x.size)
    for (i in nearestKeys.indices) {
        nearestKeys[i] = nkl[x[i]][y[i]]
    }
    return nearestKeys
}