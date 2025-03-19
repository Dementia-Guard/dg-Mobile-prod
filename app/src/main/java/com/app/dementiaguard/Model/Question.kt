package com.app.dementiaguard.Model

data class Question(
    val question: String,
    val possible_answers: List<String>? = null,
    val correct_answer: Any,  // Can be a String, List<String>, or List<Int>
    val category: String,
    val words: List<String>? = null,
    val title: String? = null,
    val article: String? = null,
    val sub_question: String? = null,
    val link_of_img: String? = null
)
