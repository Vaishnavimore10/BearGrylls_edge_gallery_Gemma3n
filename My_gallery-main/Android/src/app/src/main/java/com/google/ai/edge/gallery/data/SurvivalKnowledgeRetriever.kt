package com.google.ai.edge.gallery.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.mediapipe.tasks.components.containers.Embedding
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SurvivalKnowledgeRetriever(private val context: Context) {
    private val TAG = "SurvivalRetriever"

    // --- START OF CHANGES ---

    // 1. Properties are now 'lateinit' because they will be initialized
    // later in a background thread, not immediately in the constructor.
    private lateinit var textEmbedder: TextEmbedder
    private lateinit var knowledgeBase: List<SurvivalTip>
    private lateinit var tipEmbeddings: Map<String, Embedding>

    // 2. A public flag to let the ViewModel know if the retriever is ready to be used.
    // 'private set' means only this class can change its value.
    var isInitialized = false
        private set

    // 3. The init block is now empty. All work is moved to the new initialize() function.
    init {
        // The constructor is now lightweight and does nothing.
    }

    /**
     * Performs the heavy initialization on a background thread.
     * This is a 'suspend' function, so it must be called from a coroutine.
     */
    suspend fun initialize() {
        // Prevent running initialization more than once.
        if (isInitialized) return

        // Use Dispatchers.IO for I/O-bound tasks like reading files from assets.
        withContext(Dispatchers.IO) {
            try {
                // Create BaseOptions to configure the model path.
                val baseOptions = BaseOptions.builder()
                    .setModelAssetPath("mobilebert_embedder.tflite")
                    .build()

                // Create TextEmbedderOptions, passing in the base options.
                val options = TextEmbedder.TextEmbedderOptions.builder()
                    .setBaseOptions(baseOptions)
                    .build()

                // Create the TextEmbedder from the combined options.
                textEmbedder = TextEmbedder.createFromOptions(context, options)
                Log.d(TAG, "TextEmbedder initialized successfully.")

                // Load and parse the JSON knowledge base.
                val jsonString = context.assets.open("survival_tips.json").bufferedReader().use { it.readText() }
                val tipsArray = Gson().fromJson(jsonString, Array<SurvivalTip>::class.java)
                knowledgeBase = tipsArray.map { it.copy(id = "${it.mainCategory}_${it.subCategory}") }
                Log.d(TAG, "Loaded ${knowledgeBase.size} survival tips from JSON.")

                // Generate embeddings for every tip in the knowledge base.
                tipEmbeddings = knowledgeBase.associate { tip ->
                    val embeddingResult = textEmbedder.embed(tip.searchableContent)
                    tip.id to embeddingResult.embeddingResult().embeddings()[0]
                }
                Log.d(TAG, "Generated embeddings for all tips.")

                // Set the flag to true only after everything has succeeded.
                isInitialized = true

            } catch (e: Exception) {
                // If any error occurs, log it and keep the retriever disabled.
                Log.e(TAG, "CRITICAL: Failed to initialize SurvivalKnowledgeRetriever", e)
                isInitialized = false
            }
        }
    }

    // --- END OF CHANGES ---

    /**
     * Finds the top N most relevant survival tips for a given user query.
     */
    suspend fun findRelevantTips(userQuery: String, topN: Int = 2): List<SurvivalTip> {
        // Add a safety check. If not initialized, return an empty list immediately.
        if (!isInitialized) {
            Log.w(TAG, "Retriever not ready. Cannot find tips.")
            return emptyList()
        }

        return withContext(Dispatchers.Default) {
            // 1. Generate an embedding for the user's query in real-time
            val queryEmbeddingResult = textEmbedder.embed(userQuery)
            val queryEmbedding = queryEmbeddingResult.embeddingResult().embeddings()[0]

            // 2. Calculate similarity between the query and every tip
            val similarities = knowledgeBase.map { tip ->
                val tipEmbedding = tipEmbeddings[tip.id]!!
                val similarity = TextEmbedder.cosineSimilarity(queryEmbedding, tipEmbedding)
                tip to similarity
            }

            // 3. Sort by similarity and take the best N results
            similarities.sortedByDescending { it.second }.take(topN).map { it.first }
        }
    }
}