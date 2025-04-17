package io.github.proshian.neuralswipetyping.keyboardGrid

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.io.InputStreamReader

class KeyboardGridReader(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(KeyboardKey::class) {
                subclass(KeyboardKey.CharacterKey::class, KeyboardKey.CharacterKey.serializer())
                subclass(KeyboardKey.ActionKey::class, KeyboardKey.ActionKey.serializer())
            }
        }
    }

    fun readKeyboardGridFromAssets(filename: String): KeyboardGrid {
        return context.assets.open(filename).use { inputStream ->
            val jsonString = InputStreamReader(inputStream).readText()
            json.decodeFromString<KeyboardGrid>(jsonString)
        }
    }
}


