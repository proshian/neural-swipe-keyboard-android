package io.github.proshian.neuralswipetyping.assetUtils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object AssetUtils {
    /**
     * Copies an asset file from the app's assets to internal storage and returns its absolute path.
     *
     * This is useful when a library requires a filesystem path.
     * The file is only copied once; subsequent calls return the existing path.
     *
     * @param context The Android context to access assets and files.
     * @param assetName The relative path to the asset (e.g., "models/model_name.pte").
     * @return The absolute filesystem path to the copied file.
     * @throws IOException If the asset doesn't exist or copying fails.
     */
    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        file.parentFile?.mkdirs()
        if (file.exists() && file.length() > 0) return file.absolutePath
        context.assets.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}