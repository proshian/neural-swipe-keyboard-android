package com.example.executorch_neuroswipe_example_1.assertUtils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object AssetUtils {
    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath
        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}