# 🤖 AI Survival Guide (Gemma-Powered Android App)



![Normal Mode](https://github.com/user-attachments/assets/2273010d-96b2-46f5-8fef-c766368c72ae)

![BearGrylls Mode](https://github.com/user-attachments/assets/0271fd19-dcec-49b4-8883-81d2fded0d38)


This is a proof-of-concept Android application that demonstrates how to build a sophisticated, on-device AI chat assistant. The app leverages Google's Gemma models and advanced AI techniques like Retrieval-Augmented Generation (RAG) to act as a survival expert, powered by a custom knowledge base.

This project was built to explore the capabilities of running large language models and complex AI pipelines directly on an Android device.

## ✨ Features

*   **On-Device LLM Chat:** Interact with a Google Gemma model running entirely locally on your device.
*   **Retrieval-Augmented Generation (RAG):** The app doesn't just use the model's built-in knowledge. It first searches a local `survival_tips.json` knowledge base to find relevant information and then uses that context to provide accurate, specialized answers.
*   **Persona Prompting:** Includes a "Bear Grylls Mode" to instruct the LLM to adopt a specific persona for its responses.
*   **Dynamic Model Management:** Fetches a list of available models and allows users to download them from within the app.
*   **Hugging Face Integration:** Uses OAuth to authenticate with Hugging Face for downloading gated models.
*   **Modern Android Tech Stack:** Built with Kotlin, Jetpack Compose, Coroutines, and Hilt for dependency injection.

## 📸 Screenshots

**(IMPORTANT: Replace these placeholders with your own screenshots!)**

| Chat Screen | Model Management | Bear Grylls Mode |
| :---: | :---: | :---: |
| *(Your screenshot of the main chat interface)* | *(Your screenshot of the model download list)* | *(Your screenshot showing the "Bear Grylls Mode on" message)* |

## 🧠 Architecture: The Two-Model RAG System

This app's core intelligence comes from a powerful two-model system that enables Retrieval-Augmented Generation (RAG). Think of it as a team of a **Librarian** and an **Author**.

1.  **The Librarian (`mobilebert.tflite`):** This is a small, fast Text Embedding model from MediaPipe. Its only job is to understand the *meaning* of text. When you ask a question, the Librarian reads it and instantly finds the most relevant "book" (or tip) from the `survival_tips.json` knowledge base.

2.  **The Author (Gemma LLM):** This is the large, powerful chat model. It receives the user's question *plus* the relevant information found by the Librarian. With this extra context, the Author can write a detailed, accurate, and helpful response.

### How It Works:
1.  **Retrieve:** User asks, "How can I make water safe to drink?" The Librarian (`MobileBERT`) reads this and finds the "Use Water-Purifying Plants" tip in the JSON file is the most relevant.
2.  **Augment:** The system creates a new, hidden prompt: *"Context: [Text about Moringa seeds...]. Question: How can I make water safe to drink?"*
3.  **Generate:** This full prompt is sent to the Gemma model, which then generates a high-quality answer based on your custom knowledge.

## 🛠️ Tech Stack

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-guide.html)
*   **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
*   **AI - On-Device Inference:**
    *   **Text Embedding (RAG):** [MediaPipe TextEmbedder](httpss://developers.google.com/mediapipe/solutions/text/text_embedder/android) with `mobilebert.tflite`.
    *   **Generative Model:** Google Gemma models.
*   **Networking:** [Retrofit](https://square.github.io/retrofit/) (likely used by the model downloader) & [OkHttp](https://square.github.io/okhttp/).
*   **JSON Parsing:** [Gson](https://github.com/google/gson)

## 🚀 Getting Started

### Prerequisites
*   Android Studio (latest version recommended, e.g., Iguana or newer)
*   An Android device or emulator with Android API level 24+
*   A Hugging Face account

### Setup Instructions
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Vaishnavimore10/BearGrylls_edge_gallery_Gemma3n.git
    ```
2.  **Open in Android Studio:**
    *   Open Android Studio.
    *   Select `File` -> `Open` and navigate to the cloned project folder.
    *   Let Gradle sync and build the project. This may take a few minutes.

3.  **Configure Hugging Face OAuth (CRITICAL):**
    *   You need to create your own OAuth app on Hugging Face to allow model downloads.
    *   Go to `AuthConfig.kt` and replace the placeholder `clientId` with your own.
    *   Go to your app's `build.gradle.kts` file and ensure the `manifestPlaceholders["appAuthRedirectScheme"]` matches the scheme of the redirect URI you configured on Hugging Face.

4.  **Build and Run:**
    *   Connect your Android device or start an emulator.
    *   Click the "Run" button in Android Studio.

5.  **Download Models:**
    *   Inside the app, navigate to the model management screen.
    *   You may be prompted to log in to Hugging Face.
    *   Download a Gemma model to begin chatting.

## 📂 Project Structure

Here are the key files and directories we created for the core functionality:
