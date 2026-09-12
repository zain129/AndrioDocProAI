package com.andriosol.andriodocpro.rag.client

import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Fallback used when no real LLM is configured. It does not generate text; it returns the
 * retrieved context so the retrieval half of the pipeline can be exercised without a token.
 * Set `rag.llm.provider=huggingface` (+ HF_TOKEN) to answer with Llama.
 */
@Singleton
@Requires(property = "rag.llm.provider", value = "local", defaultValue = "local")
class LocalLlmClient : LlmClient {

    override fun answer(systemPrompt: String, userPrompt: String): String {
        log.debug("Local LLM fallback answering (no generation; echoing retrieved context)")
        val context = userPrompt
            .substringAfter("Context:", "")
            .substringBefore("\n\nQuestion:")
            .trim()
        return buildString {
            append("[No LLM configured — set rag.llm.provider=huggingface and HF_TOKEN to answer with Llama.]\n\n")
            append("Most relevant retrieved context:\n")
            append(context)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(LocalLlmClient::class.java)
    }
}
