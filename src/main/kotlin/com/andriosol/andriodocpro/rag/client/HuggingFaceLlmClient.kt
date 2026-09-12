package com.andriosol.andriodocpro.rag.client

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Calls a Hugging Face-hosted chat model (default meta-llama/Llama-3.3-70B-Instruct) through the
 * OpenAI-compatible router endpoint.
 */
@Singleton
@Requires(property = "rag.llm.provider", value = "huggingface")
class HuggingFaceLlmClient(
    private val objectMapper: ObjectMapper,
    @Value("\${rag.llm.model:meta-llama/Llama-3.3-70B-Instruct}") private val model: String,
    @Value("\${rag.llm.max-tokens:512}") private val maxTokens: Int,
    @Value("\${rag.llm.temperature:0.2}") private val temperature: Double,
    @Value("\${rag.huggingface.chat-url:https://router.huggingface.co/v1/chat/completions}") private val chatUrl: String,
    @Value("\${rag.huggingface.api-token:}") private val token: String
) : LlmClient {

    private val http: HttpClient = HttpClient.newHttpClient()

    override fun answer(systemPrompt: String, userPrompt: String): String {
        check(token.isNotBlank()) {
            "rag.huggingface.api-token (env HF_TOKEN) is required for the huggingface llm provider"
        }

        val payload = objectMapper.writeValueAsString(
            mapOf(
                "model" to model,
                "messages" to listOf(
                    mapOf("role" to "system", "content" to systemPrompt),
                    mapOf("role" to "user", "content" to userPrompt)
                ),
                "max_tokens" to maxTokens,
                "temperature" to temperature
            )
        )
        val request = HttpRequest.newBuilder(URI.create(chatUrl))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()

        log.debug("Calling HF chat model '{}' (prompt: {} chars, max_tokens: {})", model, userPrompt.length, maxTokens)
        val start = System.currentTimeMillis()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            log.error("HF chat request to '{}' failed with HTTP {} after {} ms", model, response.statusCode(), System.currentTimeMillis() - start)
        }
        check(response.statusCode() in 200..299) {
            "Hugging Face chat request failed (${response.statusCode()}): ${response.body()}"
        }
        log.debug("HF chat model '{}' responded in {} ms", model, System.currentTimeMillis() - start)

        val parsed = objectMapper.readValue(response.body(), ChatCompletionResponse::class.java)
        return parsed.choices.firstOrNull()?.message?.content?.trim()
            ?: throw IllegalStateException("Hugging Face chat response contained no choices")
    }

    companion object {
        private val log = LoggerFactory.getLogger(HuggingFaceLlmClient::class.java)
    }

    @Serdeable
    data class ChatCompletionResponse(val choices: List<Choice> = emptyList())

    @Serdeable
    data class Choice(val message: Message)

    @Serdeable
    data class Message(val role: String? = null, val content: String? = null)
}
