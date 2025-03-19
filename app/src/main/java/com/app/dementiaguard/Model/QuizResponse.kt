package com.app.dementiaguard.Model

data class QuizResponse(
    val question: String,
    val answer: Any?,
    val hint: String?,
    val quiz_done: Boolean,
    val image_base64: String?,
    val is_correct:Boolean?
) {
    fun getAnswerAsString(): String {
        return when (answer) {
            is String -> answer
            is List<*> -> (answer as List<*>).joinToString(", ") { it.toString() }
            else -> answer.toString()
        }
    }
}
