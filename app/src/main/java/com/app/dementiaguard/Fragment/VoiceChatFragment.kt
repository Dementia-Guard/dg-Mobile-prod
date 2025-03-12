package com.app.dementiaguard.Fragment

import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dementiaguard.Adapters.ChatAdapter
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.SpeechHelper
import com.app.dementiaguard.Utils.TextToSpeechHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VoiceChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VoiceChatFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var btnSpeak: Button
    private lateinit var ivGif: ImageView
    private lateinit var ivAssistantGif: ImageView

    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private lateinit var speechHelper: SpeechHelper
    private lateinit var textToSpeechHelper: TextToSpeechHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_voice_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvChat = view.findViewById(R.id.rvChat)
        btnSpeak = view.findViewById(R.id.btnSpeak)
        ivGif = view.findViewById(R.id.ivGif) // GIF for patient speaking
        ivAssistantGif = view.findViewById(R.id.ivAssistant) // GIF for assistant thinking

        rvChat.layoutManager = LinearLayoutManager(requireContext())
        chatAdapter = ChatAdapter(chatMessages)
        rvChat.adapter = chatAdapter

        speechHelper = SpeechHelper(requireContext()) { text ->
            onPatientResponse(text)
        }

        textToSpeechHelper = TextToSpeechHelper(requireContext())

        fetchNextQuestion()

        btnSpeak.setOnClickListener {
            startListening()
        }
    }

    private fun startListening() {
        ivGif.visibility = View.VISIBLE // Show speaking GIF
        speechHelper.startListening()
    }

    private fun onPatientResponse(response: String) {
        ivGif.visibility = View.GONE // Hide speaking GIF

        chatMessages.add(ChatMessage(response, isUser = true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        rvChat.scrollToPosition(chatMessages.size - 1)

        sendResponseToApi(response)
    }

    private fun fetchNextQuestion() {
        ivAssistantGif.visibility = View.VISIBLE // Show assistant thinking GIF

        // Simulate API call
        rvChat.postDelayed({
            ivAssistantGif.visibility = View.GONE // Hide assistant GIF

            val question = "Who are the people in the image from Graduation Day?"
            chatMessages.add(ChatMessage(question, isUser = false, imageRes = R.drawable.ic_image_placeholder))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            rvChat.scrollToPosition(chatMessages.size - 1)

            textToSpeechHelper.speak(question)

        }, 2000)
    }

    private fun sendResponseToApi(response: String) {
        // Simulate sending response to API
        rvChat.postDelayed({
            fetchNextQuestion()
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper.destroy()
        textToSpeechHelper.destroy()
    }
}