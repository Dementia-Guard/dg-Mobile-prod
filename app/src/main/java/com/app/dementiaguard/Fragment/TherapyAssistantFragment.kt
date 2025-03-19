package com.app.dementiaguard.Fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dementiaguard.Adapter.ChatMessageAdapter
import com.app.dementiaguard.Api.ChatApiService
import com.app.dementiaguard.Api.RetrofitClient
import com.app.dementiaguard.Model.ChatContinueResponse
import com.app.dementiaguard.Model.ChatMessage
import com.app.dementiaguard.Model.ChatRequest
import com.app.dementiaguard.Model.ChatResponse
import com.app.dementiaguard.Model.QuizRequest
import com.app.dementiaguard.Model.QuizResponse
import com.app.dementiaguard.R
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.util.Timer
import java.util.UUID
import kotlin.concurrent.timerTask
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

class TherapyAssistantFragment : Fragment(), TextToSpeech.OnInitListener {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSpeak: MaterialButton
    private lateinit var chatAdapter: ChatMessageAdapter
    private val chatMessages = ArrayList<ChatMessage>()
    
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech
    private val RECORD_AUDIO_REQUEST_CODE = 101
    private var isTtsReady = false
    
    private val chatApiService = RetrofitClient.instance.create(ChatApiService::class.java)
    private var currentApiCall: Call<*>? = null
    private var currentSessionId: Int? = null
    
    private var isQuizDone = true
    private var currentQuizAnswer: String? = null
    private var currentQuizHint: String? = null
    private var isHintDisplayed = false

    private var totalScore = 0f  // Track cumulative score
    private var totalQuestions = 0  // Track number of questions answered
    
    private lateinit var talkingAnimationContainer: FrameLayout
    private lateinit var talkingAnimation: View
    private lateinit var listeningAnimationContainer: FrameLayout
    private lateinit var listeningAnimation: View
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_therapy_assistant, container, false)
        
        recyclerView = view.findViewById(R.id.rvChatMessages)
        btnSpeak = view.findViewById(R.id.btnSpeak)
        talkingAnimationContainer = view.findViewById(R.id.talkingAnimationContainer)
        talkingAnimation = view.findViewById(R.id.talkingAnimation)
        listeningAnimationContainer = view.findViewById(R.id.listeningAnimationContainer)
        listeningAnimation = view.findViewById(R.id.listeningAnimation)
        
        setupRecyclerView()
        setupSpeechRecognizer()
        setupTextToSpeech()
        
        btnSpeak.setOnClickListener {
            if (checkAudioPermission()) {
                startSpeechRecognition()
            } else {
                requestAudioPermission()
            }
        }
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        if (chatMessages.isEmpty()) {
            startChatSession()
        }
    }
    
    override fun onPause() {
        super.onPause()
        currentApiCall?.cancel()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        currentApiCall?.cancel()
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatMessageAdapter(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter
    }
    
    private fun setupTextToSpeech() {
        textToSpeech = TextToSpeech(requireContext(), this)
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                activity?.runOnUiThread {
                    btnSpeak.visibility = View.GONE
                    talkingAnimationContainer.visibility = View.VISIBLE
                }
            }
            
            override fun onDone(utteranceId: String?) {
                activity?.runOnUiThread {
                    talkingAnimationContainer.visibility = View.GONE
                    btnSpeak.visibility = View.VISIBLE
                    btnSpeak.isEnabled = true
                }
            }
            
            override fun onError(utteranceId: String?) {
                activity?.runOnUiThread {
                    talkingAnimationContainer.visibility = View.GONE
                    btnSpeak.visibility = View.VISIBLE
                    btnSpeak.isEnabled = true
                    Toast.makeText(context, "TTS Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.getDefault())
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "Language not supported for TTS", Toast.LENGTH_SHORT).show()
            } else {
                isTtsReady = true
            }
        } else {
            Toast.makeText(context, "TTS Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startChatSession() {
        btnSpeak.isEnabled = false
        totalScore = 0f  // Reset at session start
        totalQuestions = 0  // Reset at session start
        
        val startChatCall = chatApiService.startChat(1)
        currentApiCall = startChatCall
        startChatCall.enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (!isAdded) return
                
                if (response.isSuccessful && response.body() != null) {
                    val chatResponse = response.body()!!
                    currentSessionId = chatResponse.session_id
                    addAssistantMessage(chatResponse.message)
                    speakText(chatResponse.message)
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
            
            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                if (!isAdded) return
                
                if (!call.isCanceled) {
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
        })
    }
    
    private fun continueChat(userMessage: String) {
        if (currentSessionId == null) {
            Toast.makeText(context, "No active chat session", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!isQuizDone && currentQuizAnswer != null) {
            handleQuizAnswer(userMessage)
            return
        }
        
        btnSpeak.isEnabled = false
        
        val chatRequest = ChatRequest(message = userMessage)
        val continueChatCall = chatApiService.continueChat(currentSessionId!!, chatRequest)
        currentApiCall = continueChatCall
        
        continueChatCall.enqueue(object : Callback<ChatContinueResponse> {
            override fun onResponse(call: Call<ChatContinueResponse>, response: Response<ChatContinueResponse>) {
                if (!isAdded) return
                
                if (response.isSuccessful && response.body() != null) {
                    val chatResponse = response.body()!!
                    addAssistantMessage(chatResponse.message)
                    speakText(chatResponse.message)
                    
                    if (chatResponse.quiz) {
                        startQuiz(userMessage)
                    }
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
            
            override fun onFailure(call: Call<ChatContinueResponse>, t: Throwable) {
                if (!isAdded) return
                
                if (!call.isCanceled) {
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
        })
    }
    
    private fun startQuiz(userMessage: String) {
        if (currentSessionId == null) return
        var message = userMessage
        if(userMessage.equals("yes", ignoreCase = true))
            message = "start quiz"
        val quizRequest = QuizRequest(answer = message)
        val quizCall = chatApiService.submitQuizAnswer(currentSessionId!!, quizRequest)
        currentApiCall = quizCall
        
        quizCall.enqueue(object : Callback<QuizResponse> {
            override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                if (!isAdded) return
                
                if (response.isSuccessful && response.body() != null) {
                    val quizResponse = response.body()!!
                    processQuizResponse(quizResponse)
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
            
            override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                if (!isAdded) return
                
                if (!call.isCanceled) {
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
        })
    }
    
    private fun processQuizResponse(quizResponse: QuizResponse) {
        isQuizDone = quizResponse.quiz_done
        currentQuizAnswer = quizResponse.getAnswerAsString()
        currentQuizHint = quizResponse.hint
        isHintDisplayed = false
        
        val messageToDisplay = quizResponse.question ?: ""
        if (messageToDisplay.isNotEmpty()) {
            addAssistantMessage(messageToDisplay)
            speakText(messageToDisplay)
        }
        
        if (quizResponse.image_base64 != null) {
            try {
                val imageBytes = Base64.decode(quizResponse.image_base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val imageResource = chatMessages.size - 1
                
                if (imageResource >= 0 && messageToDisplay.isNotEmpty()) {
                    chatMessages[imageResource] = ChatMessage(messageToDisplay, false, null, bitmap)
                    chatAdapter.notifyItemChanged(imageResource)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error displaying image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        if (quizResponse.quiz_done) {
            totalScore = 0f
            totalQuestions = 0
            isQuizDone = true
            currentQuizAnswer = null
            currentQuizHint = null
        }
    }
    
    private fun handleQuizAnswer(userAnswer: String) {
        if (currentQuizAnswer == null) return
        
        submitQuizAnswer(userAnswer)
    }
    
    private fun submitQuizAnswer(answer: String) {
        if (currentSessionId == null) return

        println("Sending quiz request: session_id=$currentSessionId, answer='$answer'")
        val quizRequest = QuizRequest(answer = answer)
        val quizCall = chatApiService.submitQuizAnswer(currentSessionId!!, quizRequest)
        currentApiCall = quizCall
        
        quizCall.enqueue(object : Callback<QuizResponse> {
            override fun onResponse(call: Call<QuizResponse>, response: Response<QuizResponse>) {
                if (!isAdded) return

                println("Raw response: ${response.raw()}")
                if (!response.isSuccessful) {

                    val errorBody = response.errorBody()?.use { it.string() } ?: "Unknown error"
                    val errorMessage = try {
                        JSONObject(errorBody).getString("message")
                    } catch (e: Exception) {
                        "Error: ${response.code()} - $errorBody"
                    }
                    println("Error: ${response.code()} - $errorBody")
                    addAssistantMessage(errorMessage)  // Display error to user
                    speakText(errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                    return
                }

                val quizResponse = response.body()
                if (quizResponse == null) {
                    val errorMessage = "Error: Failed to parse quiz response"
                    println(errorMessage)
                    addAssistantMessage(errorMessage)
                    speakText(errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                    return
                }

                totalQuestions++
                quizResponse.similarity_score?.let { totalScore += it }

                val feedback = if (quizResponse.similarity_score != null && quizResponse.similarity_score < 0.7) {
                    if (!isHintDisplayed && quizResponse.hint != null) {
                        isHintDisplayed = true
                        "Here's a hint: ${quizResponse.hint}"
                    } else {
                        "Not quite right. The answer was '${quizResponse.getAnswerAsString()}'. Let's move on."
                    }

                } else {
                    if (quizResponse.similarity_score != null) {
                        "Good answer! Similarity: ${String.format("%.2f", quizResponse.similarity_score * 100)}%"
                    } else "Correct!"
                }
                addAssistantMessage(feedback)
                speakText(feedback)

                // Process the next question immediately if present
                if (quizResponse.question != null && !quizResponse.quiz_done) {
                    processQuizResponse(quizResponse)
                } else if (quizResponse.quiz_done) {
                    processQuizResponse(quizResponse)  // Handle quiz completion
                }

            }
            
            override fun onFailure(call: Call<QuizResponse>, t: Throwable) {
                if (!isAdded) return
                
                if (!call.isCanceled) {
                    println("Network error: ${t.message}")
                    val errorMessage = "Network error: ${t.message}"
                    addAssistantMessage(errorMessage)
                    speakText(errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    btnSpeak.isEnabled = true
                }
            }
        })
    }
    
    private fun speakText(text: String) {
        if (isTtsReady && isAdded) {
            btnSpeak.isEnabled = false
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }
    
    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    btnSpeak.visibility = View.GONE
                    listeningAnimationContainer.visibility = View.VISIBLE
                }
                
                override fun onBeginningOfSpeech() {}
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    listeningAnimationContainer.visibility = View.GONE
                    btnSpeak.visibility = View.VISIBLE
                    btnSpeak.text = "Speak"
                    btnSpeak.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellowish_text))
                }
                
                override fun onError(error: Int) {
                    listeningAnimationContainer.visibility = View.GONE
                    btnSpeak.visibility = View.VISIBLE
                    btnSpeak.text = "Speak"
                    btnSpeak.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellowish_text))
                    
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    
                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
                
                override fun onResults(results: Bundle?) {
                    listeningAnimationContainer.visibility = View.GONE
                    btnSpeak.visibility = View.VISIBLE
                    btnSpeak.text = "Speak"
                    btnSpeak.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellowish_text))
                    
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        addUserMessage(recognizedText)
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
            btnSpeak.isEnabled = false
        }
    }
    
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireActivity().packageName)
        
        speechRecognizer.startListening(intent)
    }
    
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition()
            } else {
                Toast.makeText(
                    context,
                    "Permission denied. Speech recognition requires microphone access.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun addAssistantMessage(message: String) {
        chatMessages.add(ChatMessage(message, false))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)
    }
    
    private fun addUserMessage(message: String) {
        chatMessages.add(ChatMessage(message, true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        recyclerView.scrollToPosition(chatMessages.size - 1)
        
        continueChat(message)
    }
}
