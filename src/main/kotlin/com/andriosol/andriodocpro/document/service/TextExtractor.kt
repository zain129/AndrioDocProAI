package com.andriosol.andriodocpro.document.service

import jakarta.inject.Singleton
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Extracts plain text from an uploaded document using Apache Tika,
 * which auto-detects the format (PDF/DOC/DOCX) and delegates to the right parser.
 */
@Singleton
class TextExtractor {

    // AutoDetectParser is thread-safe and can be shared; handler/metadata are per-call.
    private val parser = AutoDetectParser()

    fun extract(input: InputStream, filename: String): String {
        val start = System.currentTimeMillis()
        // -1 removes Tika's default 100k character write limit so large documents are not truncated.
        val handler = BodyContentHandler(-1)
        val metadata = Metadata().apply {
            set(TikaCoreProperties.RESOURCE_NAME_KEY, filename)
        }
        input.use { parser.parse(it, handler, metadata, ParseContext()) }
        val text = handler.toString()
        log.debug(
            "Tika extracted {} chars from '{}' (detected type: {}) in {} ms",
            text.length, filename, metadata.get("Content-Type"), System.currentTimeMillis() - start
        )
        return text
    }

    companion object {
        private val log = LoggerFactory.getLogger(TextExtractor::class.java)
    }
}
