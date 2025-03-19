package com.app.dementiaguard.Activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Fragment.QuestionSession.*
import com.app.dementiaguard.Model.EvaluationRequest
import com.app.dementiaguard.Model.EvaluationResponse
import com.app.dementiaguard.Model.QuestionRequest
import com.app.dementiaguard.Model.SessionResponse
import com.app.dementiaguard.R
import com.app.dementiaguard.Service.RetrofitService
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuestionSession : AppCompatActivity() {
    private lateinit var sessionResponse: SessionResponse
    private var currentQuestionIndex = 0
    private val questionList by lazy { sessionResponse.questions }
    private val timeSpentList = mutableListOf<Long>()
    private val userAnswers = mutableListOf<String?>()
    private lateinit var questionSessionTitle: TextView
    private lateinit var questionSessionInfo: MaterialCardView
    private val retrofitService = RetrofitService()
    private var isMemoryRecallPhase = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_question_session)

        questionSessionTitle = findViewById(R.id.txtQuestionSessionHeader)
        questionSessionInfo = findViewById(R.id.cvNotiFb)

        // Show loading fragment initially
        showLoadingFragment()

        // Fetch session data
        fetchSessionData()
    }

    private fun showLoadingFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionSessionFragmentContainer, LoadingFragment.newInstance())
            .commit()
    }

    private fun fetchSessionData() {
        CoroutineScope(Dispatchers.IO).launch {
            val apiService = retrofitService.createApiService()
            val requestBody = mapOf("user_id" to 1)

            try {
                val response = apiService.createQuestionSession(requestBody)
                if (response.isSuccessful) {
                    response.body()?.let { session ->
                        sessionResponse = session
                        withContext(Dispatchers.Main) {
                            if (questionList.isNotEmpty()) {
                                loadQuestionFragment()
                            } else {
                                showErrorDialog("No questions available in the session.")
                            }
                        }
                    } ?: run {
                        withContext(Dispatchers.Main) {
                            showErrorDialog("Empty response from server.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showErrorDialog("Failed to fetch session data: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showErrorDialog("Network error: ${e.message}")
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Session")
            .setMessage("Do you want to exit from the question session?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun loadQuestionFragment() {
        val question = questionList[currentQuestionIndex]
        val difficultyLevel = sessionResponse.difficulty_level.toInt()
        val totalQuestions = questionList.size
        val fragment: Fragment = when {
            question.category == "memory_recall" && !isMemoryRecallPhase -> {
                MemoryRecallFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions, false)
            }
            question.category == "memory_recall" && isMemoryRecallPhase -> {
                MemoryRecallFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions, true)
            }
            question.category == "date_questions" -> DayTodayFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions)
            question.category == "object_recall" -> ObjectRecallFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions)
            question.category == "backward_count" -> BackwardFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions)
            question.category == "problem_solving" -> ProblemSolvingFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions)
            question.category == "article" -> ArticleFragment.newInstance(question, difficultyLevel, currentQuestionIndex, totalQuestions)
            else -> throw IllegalArgumentException("Invalid category: ${question.category}")
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.questionSessionFragmentContainer, fragment)
            .commit()
    }

    private fun loadResultsFragment(evaluationResponse: EvaluationResponse) {
        val fragment = ResultsFragment.newInstance(timeSpentList, evaluationResponse) // Adjust ResultsFragment to accept EvaluationResponse
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionSessionFragmentContainer, fragment)
            .commit()
    }

    private fun evaluateSessionAndLoadResults() {
        val request = createEvaluationRequest()
        val answersJson = Gson().toJson(request) // For logging
        println("Sending to evaluate-session: $answersJson")

        // Show loading fragment while evaluating
        showLoadingFragment()

        CoroutineScope(Dispatchers.IO).launch {
            val apiService = retrofitService.createApiService()
            try {
                val response = apiService.evaluateSession(request)
                println("response $response")
                if (response.isSuccessful) {
                    response.body()?.let { evaluation ->
                        withContext(Dispatchers.Main) {
                            questionSessionTitle.visibility = View.GONE
                            questionSessionInfo.visibility = View.GONE
                            loadResultsFragment(evaluation)
                        }
                    } ?: run {
                        withContext(Dispatchers.Main) {
                            showErrorDialog("Empty evaluation response from server.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showErrorDialog("Failed to evaluate session: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    showErrorDialog("Network error during evaluation: ${e.message}")
                }
            }
        }
    }

    fun nextQuestion() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.questionSessionFragmentContainer)
        val elapsedTime = when (currentFragment) {
            is DayTodayFragment -> currentFragment.getElapsedTime()
            is ObjectRecallFragment -> currentFragment.getElapsedTime()
            is BackwardFragment -> currentFragment.getElapsedTime()
            is MemoryRecallFragment -> currentFragment.getElapsedTime()
            is ProblemSolvingFragment -> currentFragment.getElapsedTime()
            is ArticleFragment -> currentFragment.getElapsedTime()
            else -> 0L
        }

        val userAnswer = when (currentFragment) {
            is DayTodayFragment -> currentFragment.getUserAnswer()
            is ObjectRecallFragment -> currentFragment.getUserAnswer()
            is BackwardFragment -> currentFragment.getUserAnswer()
            is MemoryRecallFragment -> currentFragment.getUserAnswer()
            is ProblemSolvingFragment -> currentFragment.getUserAnswer()
            is ArticleFragment -> currentFragment.getUserAnswer()
            else -> null
        }

        // Check if this is the initial MemoryRecallFragment phase (no answer required)
        val isInitialMemoryRecall = currentFragment is MemoryRecallFragment && !isMemoryRecallPhase

        // If not the initial MemoryRecall phase, require an answer
        if (!isInitialMemoryRecall && (userAnswer.isNullOrEmpty() || userAnswer == "")) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Information")
                .setMessage("Please provide an answer")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            // Store the time and answer
            timeSpentList.add(elapsedTime)
            userAnswers.add(userAnswer ?: "") // Store empty string if null for initial phase

            if (isMemoryRecallPhase) {
                // In recall phase, proceed to evaluation
                if (userAnswers.size > 1 && userAnswers[0].isNullOrEmpty()) {
                    userAnswers[0] = userAnswers.last() // Replace first element with the last
                }
                evaluateSessionAndLoadResults()
            } else if (currentQuestionIndex < questionList.size - 1) {
                // Move to the next question
                currentQuestionIndex++
                loadQuestionFragment()
            } else {
                // After all questions, switch to recall phase for memory_recall
                isMemoryRecallPhase = true
                currentQuestionIndex = 0 // Reset to first question (memory_recall)
                loadQuestionFragment()
            }
        }
    }

    private fun createEvaluationRequest(): EvaluationRequest {
        val totalTime = timeSpentList.sum()
        val questions = questionList.mapIndexed { index, question ->
            QuestionRequest(
                question = question.question,
                category = question.category,
                userAnswer = userAnswers[index],
                subQuestion = if (question.category == "memory_recall") question.sub_question else null,
                correctAnswer = when (question.category) {
                    "memory_recall" -> question.correct_answer ?: question.words
                    "object_recall" -> question.correct_answer
                    "date_questions" -> question.correct_answer
                    "backward_count" -> question.correct_answer ?: question.possible_answers
                    "problem_solving" -> question.correct_answer
                    "article" -> question.correct_answer
                    else -> null
                },
                options = if (question.category in listOf("object_recall", "date_questions", "problem_solving")) question.possible_answers else null,
                article = if (question.category == "article") question.article else null,
                title = if (question.category == "article") question.title else null
            )
        }

        return EvaluationRequest(
            sessionId = sessionResponse.session_id,
            userId = sessionResponse.user_id.toInt(), // Assuming user_id is String in SessionResponse, convert to Int
            difficultyLevel = sessionResponse.difficulty_level,
            totalTime = totalTime,
            questions = questions
        )
    }
}