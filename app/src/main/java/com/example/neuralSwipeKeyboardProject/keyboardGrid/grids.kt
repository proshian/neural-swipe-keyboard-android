package com.example.neuralSwipeKeyboardProject.keyboardGrid

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class KeyboardKeyHitbox(val x: Int, val y: Int, val w: Int, val h: Int)

@Serializable(with = KeyboardKeySerializer::class)
sealed class KeyboardKey {
    abstract val hitbox: KeyboardKeyHitbox

    @Serializable
    @SerialName("character")
    data class CharacterKey(
        val label: String,
        override val hitbox: KeyboardKeyHitbox
    ) : KeyboardKey()

    @Serializable
    @SerialName("action")
    data class ActionKey(
        val action: String,
        override val hitbox: KeyboardKeyHitbox
    ) : KeyboardKey()
}


@Serializable
data class KeyboardGrid(val width: Int, val height: Int, val keys: List<KeyboardKey>)



object KeyboardKeySerializer : JsonContentPolymorphicSerializer<KeyboardKey>(KeyboardKey::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out KeyboardKey> {
        return when {
            "label" in element.jsonObject -> KeyboardKey.CharacterKey.serializer()
            "action" in element.jsonObject -> KeyboardKey.ActionKey.serializer()
            else -> throw SerializationException("Unknown KeyboardKey type")
        }
    }
}
