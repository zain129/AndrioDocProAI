package com.andriosol.andriodocpro.chunk.service

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton

/**
 * Splits extracted document text into overlapping fixed-size windows.
 * Overlap keeps information that straddles a boundary retrievable from at least one chunk.
 */
@Singleton
class Chunker(
    @Value("\${rag.chunk.size:900}") private val size: Int,
    @Value("\${rag.chunk.overlap:200}") private val overlap: Int
) {
    fun chunk(text: String): List<String> {
        val clean = text.replace("\r\n", "\n").replace('\r', '\n').trim()
        if (clean.isEmpty()) return emptyList()

        val step = (size - overlap).coerceAtLeast(1)
        val chunks = ArrayList<String>()
        var start = 0
        while (start < clean.length) {
            val end = (start + size).coerceAtMost(clean.length)
            chunks.add(clean.substring(start, end).trim())
            if (end == clean.length) break
            start += step
        }
        return chunks.filter { it.isNotBlank() }
    }
}
