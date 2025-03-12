package com.app.dementiaguard.Utils

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TextToSpeechHelper(context: Context) {

    private var textToSpeech: TextToSpeech? = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.getDefault()
        }
    }

    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun destroy() {
        textToSpeech?.shutdown()
    }
}