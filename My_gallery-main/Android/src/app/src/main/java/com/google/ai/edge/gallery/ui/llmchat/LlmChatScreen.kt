/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.edge.gallery.ui.llmchat

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import com.google.ai.edge.gallery.firebaseAnalytics
import com.google.ai.edge.gallery.ui.common.chat.ChatMessageAudioClip
import com.google.ai.edge.gallery.ui.common.chat.ChatMessageImage
import com.google.ai.edge.gallery.ui.common.chat.ChatMessageText
import com.google.ai.edge.gallery.ui.common.chat.ChatView
import com.google.ai.edge.gallery.ui.modelmanager.ModelManagerViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
/** Navigation destination data */
object LlmChatDestination {
  val route = "LlmChatRoute"
}

object LlmAskImageDestination {
  val route = "LlmAskImageRoute"
}

object LlmAskAudioDestination {
  val route = "LlmAskAudioRoute"
}


@Composable
fun LlmChatScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LlmChatViewModel,
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    navigateUp = navigateUp,
    modifier = modifier,
  )
}

@Composable
fun LlmAskImageScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LlmAskImageViewModel,
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    navigateUp = navigateUp,
    modifier = modifier,
  )
}

@Composable
fun LlmAskAudioScreen(
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LlmAskAudioViewModel,
) {
  ChatViewWrapper(
    viewModel = viewModel,
    modelManagerViewModel = modelManagerViewModel,
    navigateUp = navigateUp,
    modifier = modifier,
  )
}

@Composable
fun ChatViewWrapper(
  viewModel: LlmChatViewModelBase,
  modelManagerViewModel: ModelManagerViewModel,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var isBearGryllsMode by rememberSaveable { mutableStateOf(false) }

  // The root Column now controls the overall layout
  Column(modifier = modifier) {

    // --- START OF CHANGES ---

    // NEW: We wrap the toggle switch Row in a Surface to give it a background and elevation.
    // This makes it stand out and brings it down from the absolute top edge.
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 8.dp), // Add some padding around the surface
      shadowElevation = 2.dp // Give it a slight shadow to lift it off the background
    ) {
      androidx.compose.foundation.layout.Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp) // Inner padding for the text/switch
      ) {
        Text("Bear Grylls Mode", modifier = Modifier.weight(1f))
        Switch(
          checked = isBearGryllsMode,
          onCheckedChange = { isBearGryllsMode = it }
        )
      }
    }
    // Chat view
    ChatView(
      task = viewModel.task,
      viewModel = viewModel,
      modelManagerViewModel = modelManagerViewModel,
      onSendMessage = { model, messages ->
        for (message in messages) {
          viewModel.addMessage(model = model, message = message)
        }

        var text = ""
        val images: MutableList<Bitmap> = mutableListOf()
        val audioMessages: MutableList<ChatMessageAudioClip> = mutableListOf()
        var chatMessageText: ChatMessageText? = null

        for (message in messages) {
          when (message) {
            is ChatMessageText -> {
              chatMessageText = message
              text = message.content
            }
            is ChatMessageImage -> images.addAll(message.bitmaps)
            is ChatMessageAudioClip -> audioMessages.add(message)
          }
        }

        if ((text.isNotEmpty() && chatMessageText != null) || audioMessages.isNotEmpty()) {
          modelManagerViewModel.addTextInputHistory(text)

          val modifiedInput = if (isBearGryllsMode) {
            """
              You are Bear Grylls, a world-renowned survival expert.
                    
                    Always prefix your responses with:
                    "BEAR GRYLLS: "

                    When a user gives a survival scenario, do the following:

                    1. Start by asking clarifying questions if important context is missing:
                       - "Do you have any supplies (knife, matches, phone, etc.)?"
                       - "Do you know your current location or nearby landmarks?"
                       - "What’s the weather like?"
                       - "Are you injured or with anyone else?"

                    2. If answers are already provided, use them to tailor your advice.

                    3. Provide concise, actionable survival instructions suitable for the field.

                    User scenario: $text
            """.trimIndent()
          } else {
            text
          }

          viewModel.generateResponse(
            model = model,
            input = modifiedInput,
            images = images,
            audioMessages = audioMessages,
            onError = {
              viewModel.handleError(
                context = context,
                model = model,
                modelManagerViewModel = modelManagerViewModel,
                triggeredMessage = chatMessageText,
              )
            },
          )

          firebaseAnalytics?.logEvent(
            "generate_action",
            bundleOf(
              "capability_name" to viewModel.task.type.toString(),
              "model_id" to model.name,
            )
          )
        }
      },
      onRunAgainClicked = { model, message ->
        if (message is ChatMessageText) {
          viewModel.runAgain(
            model = model,
            message = message,
            onError = {
              viewModel.handleError(
                context = context,
                model = model,
                modelManagerViewModel = modelManagerViewModel,
                triggeredMessage = message,
              )
            },
          )
        }
      },
      onBenchmarkClicked = { _, _, _, _ -> },
      onResetSessionClicked = { model -> viewModel.resetSession(model = model) },
      showStopButtonInInputWhenInProgress = true,
      onStopButtonClicked = { model -> viewModel.stopResponse(model = model) },
      navigateUp = navigateUp,
      modifier = Modifier.weight(1f)
    )
  }
}
