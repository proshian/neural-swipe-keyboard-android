package com.example.trie

import java.io.Serializable
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.ObjectInputStream
import java.io.InputStream
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

sealed class Node<T> : Serializable {
    abstract val children: Map<T, Node<T>>
}

class MutableNode<T : Serializable>(
    override val children: MutableMap<T, MutableNode<T>> = mutableMapOf()
) : Node<T>()

class ImmutableNode<T : Serializable>(
    override val children: ImmutableMap<T, ImmutableNode<T>>
) : Node<T>(), Serializable {
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(children.toMap()) // Convert to regular Map for serialization
    }

    private fun readObject(inp: ObjectInputStream) {
        @Suppress("UNCHECKED_CAST")
        val readChildren = inp.readObject() as Map<T, ImmutableNode<T>>
        val field = this::class.java.getDeclaredField("children").apply { isAccessible = true }
        field.set(this, readChildren.toImmutableMap())
    }
}

fun <T : Serializable> buildMutableTrie(tokenizedVocab: Iterable<Iterable<T>>): MutableNode<T> {
    val root = MutableNode<T>()
    tokenizedVocab.forEach { tokens ->
        var currentNode = root
        for (token in tokens) {
            currentNode.children[token] = currentNode.children.getOrPut(token) { MutableNode() }
            currentNode = currentNode.children[token]!!
        }
    }
    return root
}

fun <T : Serializable> convertToImmutable(mutableRoot: MutableNode<T>): ImmutableNode<T> {
    fun convertNode(node: MutableNode<T>): ImmutableNode<T> {
        val children = node.children.mapValues { (_, childNode) -> convertNode(childNode) }
            .toImmutableMap()
        return ImmutableNode(children)
    }
    return convertNode(mutableRoot)
}

/**
 * Traverses the trie following the given token sequence and returns allowed continuations.
 *
 * @param root The root node of the trie
 * @param inputIds Sequence of tokens representing the prefix path to traverse
 * @return Set of allowed next tokens if the `inputIds` prefix exists in the trie, `null` otherwise
 *
 * @example
 * ```
 * // Given trie containing words: ["hello", "hi"]
 * traverseTrie(trieRoot, listOf("h", "e")) // Returns setOf("l")
 * traverseTrie(trieRoot, listOf("h"))      // Returns setOf("e", "i")
 * traverseTrie(trieRoot, listOf("ha"))      // Returns null
 * ```
 */
fun <T : Serializable> traverseTrie(root: Node<T>, inputIds: List<T>): Set<T>? {
    var currentNode = root
    for (token in inputIds) {
        currentNode = currentNode.children[token] ?: return null
    }
    return currentNode.children.keys
}


fun <T : Serializable> serializeImmutableTrie(root: ImmutableNode<T>, outputStream: OutputStream) {
    ObjectOutputStream(outputStream).use { it.writeObject(root) }
}

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> deserializeImmutableTrie(inputStream: InputStream): ImmutableNode<T> {
    return ObjectInputStream(inputStream).use { it.readObject() as ImmutableNode<T> }
}