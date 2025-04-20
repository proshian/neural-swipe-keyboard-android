package io.github.proshian.neuralswipetyping.keyboardGrid

import android.content.Context
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.io.InputStreamReader


@Serializable
data class KeyboardKeyHitbox(val x: Int, val y: Int, val w: Int, val h: Int)

@Serializable(with = KeyboardKeySerializer::class)
sealed class KeyboardKey {
    abstract val hitbox: KeyboardKeyHitbox

    @Serializable
    data class CharacterKey(
        val label: String,
        override val hitbox: KeyboardKeyHitbox
    ) : KeyboardKey()

    @Serializable
    data class ActionKey(
        val action: String,
        override val hitbox: KeyboardKeyHitbox
    ) : KeyboardKey()
}


@Serializable
data class KeyboardGrid(val width: Int, val height: Int, val keys: List<KeyboardKey>)


object KeyboardKeySerializer : JsonContentPolymorphicSerializer<KeyboardKey>(KeyboardKey::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<KeyboardKey> {
        return when {
            "label" in element.jsonObject -> KeyboardKey.CharacterKey.serializer()
            "action" in element.jsonObject -> KeyboardKey.ActionKey.serializer()
            else -> throw SerializationException("Unknown KeyboardKey type")
        }
    }
}


class KeyboardGridReader(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun readKeyboardGridFromAssets(filename: String): KeyboardGrid {
        return context.assets.open(filename).use { inputStream ->
            val jsonString = InputStreamReader(inputStream).readText()
            json.decodeFromString<KeyboardGrid>(jsonString)
        }
    }
}
