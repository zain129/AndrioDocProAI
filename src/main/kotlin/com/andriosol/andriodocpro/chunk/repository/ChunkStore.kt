package com.andriosol.andriodocpro.chunk.repository

import com.andriosol.andriodocpro.chunk.model.RetrievedChunk
import io.micronaut.context.annotation.Value
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.sql.DataSource

/**
 * Handles the pgvector-specific persistence and retrieval that Micronaut Data cannot express:
 * writing chunk embeddings and running cosine-distance nearest-neighbour search.
 */
@Singleton
class ChunkStore(
    dataSource: DataSource,
    @Value("\${rag.retrieval.hnsw-ef-search:200}") private val hnswEfSearch: Int
) {

    // Micronaut wraps the pool in a transaction-aware delegate that requires an active
    // transaction; unwrap it since this store manages its own connections/transactions.
    private val dataSource: DataSource = DelegatingDataSource.unwrapDataSource(dataSource)

    /** Inserts all chunks of a document (with embeddings) in a single transaction. */
    fun insertAll(documentId: UUID, chunks: List<String>, embeddings: List<FloatArray>) {
        require(chunks.size == embeddings.size) {
            "chunk/embedding count mismatch: ${chunks.size} vs ${embeddings.size}"
        }
        dataSource.connection.use { conn ->
            val previousAutoCommit = conn.autoCommit
            conn.autoCommit = false
            try {
                conn.prepareStatement(INSERT_SQL).use { ps ->
                    for (i in chunks.indices) {
                        ps.setObject(1, UUID.randomUUID())
                        ps.setObject(2, documentId)
                        ps.setInt(3, i)
                        ps.setString(4, chunks[i])
                        ps.setString(5, embeddings[i].toPgVector())
                        ps.addBatch()
                    }
                    ps.executeBatch()
                }
                conn.commit()
                log.debug("Inserted {} chunks for document {}", chunks.size, documentId)
            } catch (e: Exception) {
                log.error("Chunk batch insert failed for document {}; rolled back", documentId, e)
                conn.rollback()
                throw e
            } finally {
                conn.autoCommit = previousAutoCommit
            }
        }
    }

    /**
     * Returns the [topK] chunks most similar to [queryEmbedding].
     *
     * [documentIds] scopes the search: `null` means unrestricted (admin / a single-document
     * ask where access was already verified by the caller), an empty set means "nothing is
     * accessible" and short-circuits without hitting the database, and a non-empty set
     * restricts results to those documents (used both for a single-document ask and for
     * scoping a non-admin's cross-document search to what they're allowed to see).
     */
    fun search(queryEmbedding: FloatArray, topK: Int, documentIds: Set<UUID>?): List<RetrievedChunk> {
        if (documentIds != null && documentIds.isEmpty()) {
            log.debug("Vector search skipped: caller has no accessible documents")
            return emptyList()
        }

        val vector = queryEmbedding.toPgVector()
        val sql = StringBuilder(
            """
            SELECT c.document_id, d.filename, c.chunk_index, c.content,
                   1 - (c.embedding <=> CAST(? AS vector)) AS score
            FROM document_chunks c
            JOIN documents d ON d.id = c.document_id
            WHERE c.embedding IS NOT NULL
            """.trimIndent()
        )
        if (documentIds != null) sql.append(" AND c.document_id = ANY(?)")
        sql.append(" ORDER BY c.embedding <=> CAST(? AS vector) LIMIT ?")

        val start = System.currentTimeMillis()
        dataSource.connection.use { conn ->
            // HNSW is an approximate index: with a document filter, the default ef_search
            // candidate pool can miss chunks belonging to a small document once the overall
            // corpus grows. Raising it trades a little query time for much better recall.
            conn.createStatement().use { it.execute("SET hnsw.ef_search = $hnswEfSearch") }
            conn.prepareStatement(sql.toString()).use { ps ->
                var idx = 1
                ps.setString(idx++, vector)               // score expression
                if (documentIds != null) {
                    ps.setArray(idx++, conn.createArrayOf("uuid", documentIds.toTypedArray()))
                }
                ps.setString(idx++, vector)               // order-by expression
                ps.setInt(idx, topK)
                ps.executeQuery().use { rs ->
                    val results = ArrayList<RetrievedChunk>()
                    while (rs.next()) {
                        results.add(
                            RetrievedChunk(
                                documentId = rs.getObject("document_id", UUID::class.java),
                                filename = rs.getString("filename"),
                                chunkIndex = rs.getInt("chunk_index"),
                                content = rs.getString("content"),
                                score = rs.getDouble("score")
                            )
                        )
                    }
                    log.debug(
                        "Vector search returned {}/{} chunks in {} ms (scope: {}, best score: {})",
                        results.size, topK, System.currentTimeMillis() - start,
                        documentIds?.let { "${it.size} document(s)" } ?: "all documents",
                        results.firstOrNull()?.let { "%.4f".format(it.score) } ?: "n/a"
                    )
                    return results
                }
            }
        }
    }

    private fun FloatArray.toPgVector(): String =
        joinToString(separator = ",", prefix = "[", postfix = "]")

    companion object {
        private val log = LoggerFactory.getLogger(ChunkStore::class.java)
        private const val INSERT_SQL =
            "INSERT INTO document_chunks (id, document_id, chunk_index, content, embedding, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, CAST(? AS vector), now(), now())"
    }
}
