package com.google.ai.edge.gallery.data

import com.google.gson.annotations.SerializedName

data class SurvivalTip(
    // A unique identifier for each tip, combining main and sub categories
    @SerializedName("id")
    val id: String = "", // We will generate this ourselves

    @SerializedName("main_category")
    val mainCategory: String,

    @SerializedName("sub_category")
    val subCategory: String,

    // Use @SerializedName to accept either "tips" or "tip" from the JSON
    // and map it to a single property called `tipText`.
    @SerializedName(value = "tips", alternate = ["tip"])
    val tipText: String,

    @SerializedName("instruction")
    val instruction: String
) {
    /**
     * CRUCIAL: This computed property creates the rich text block that
     * our embedding model will analyze for semantic search.
     * Combining these fields gives the best results.
     */
    val searchableContent: String
        get() = "Topic: $subCategory. Tip: $tipText. How to do it: $instruction"
}