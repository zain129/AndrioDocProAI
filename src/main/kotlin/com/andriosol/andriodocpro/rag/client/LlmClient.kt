package com.andriosol.andriodocpro.rag.client

/** Generates a natural-language answer. Implementations are selected via `rag.llm.provider`. */
interface LlmClient {
    fun answer(systemPrompt: String, userPrompt: String): String
}
