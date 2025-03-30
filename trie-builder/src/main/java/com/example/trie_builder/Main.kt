package com.example.trie_builder

import com.example.trie.* // From your shared 'trie' module
import com.example.trie_builder.tokenizers.RuSubwordTokenizer
import java.io.FileOutputStream
import java.io.File

fun main() {
    val tokenizer = RuSubwordTokenizer()
    val vocab = File("trie-builder/voc.txt").readLines()


    val tokenizedVocab = vocab.map { word ->
        tokenizer.tokenize(word).map { it.toInt() } // Ensure tokens are serializable
    }

    val trie = buildMutableTrie(tokenizedVocab)
    val immutableTrie = convertToImmutable(trie)

    FileOutputStream("app/src/main/assets/trie.ser").use { output ->
        serialize(immutableTrie, output)
    }
    println("Trie built successfully!")
}