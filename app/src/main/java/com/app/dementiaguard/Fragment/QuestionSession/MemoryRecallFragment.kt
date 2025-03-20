package com.app.dementiaguard.Fragment.QuestionSession

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Activity.QuestionSession
import com.app.dementiaguard.Model.Question
import com.app.dementiaguard.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MemoryRecallFragment : Fragment() {

    private lateinit var question: Question
    private var difficultyLevel: Int? = 0
    private var currentQuestionIndex: Int? = 0
    private var allQuestionAmount: Int? = 0
    private var isRecallPhase: Boolean = false
    private lateinit var txtTimer: TextView
    private lateinit var editAnswer: TextInputEditText
    private lateinit var editAnswerLayout: TextInputLayout
    private lateinit var txtSubQuestion: TextView
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar

    private var elapsedTime = 0L
    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedTime++
            val hours = elapsedTime / 3600
            val minutes = (elapsedTime % 3600) / 60
            val seconds = elapsedTime % 60
            val timeFormatted = String.format("Time: %02d:%02d:%02d", hours, minutes, seconds)
            txtTimer.text = timeFormatted
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memory_recall, container, false)

        val txtQuestionCount = view.findViewById<TextView>(R.id.txtQuestionCount)
        val txtQuestionAmount = view.findViewById<TextView>(R.id.TxtQuestionAmount)
        val txtQuestion = view.findViewById<TextView>(R.id.txtQuestion)
        val txtWords = view.findViewById<TextView>(R.id.txtWords)
        txtSubQuestion = view.findViewById<TextView>(R.id.txtSubQuestion)
        editAnswer = view.findViewById<TextInputEditText>(R.id.editAnswer)
        editAnswerLayout = view.findViewById<TextInputLayout>(R.id.tilUserAnswer)
        btnNext = view.findViewById<Button>(R.id.btnObjectRecallNext)
        txtTimer = view.findViewById(R.id.txtTimer)
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        // Set up question count and amount
        txtQuestionCount.text = "Question $currentQuestionIndex "
        txtQuestionAmount.text = "out of $allQuestionAmount"
        txtQuestion.text = question.question
        txtWords.text = question.words?.joinToString(", ") ?: ""

        // Update progress bar
        updateProgressBar()

        // Show sub-question and EditText only in recall phase
        if (isRecallPhase) {
            progressBar.visibility = View.GONE
            txtQuestion.visibility = View.GONE
            txtWords.visibility = View.GONE
            txtQuestionCount.text = "Question $currentQuestionIndex "
            txtQuestionAmount.text = "Recalling"
            txtSubQuestion.visibility = View.VISIBLE
            editAnswerLayout.visibility = View.VISIBLE
            editAnswer.visibility = View.VISIBLE
            txtSubQuestion.text = question.sub_question ?: "What were the words that asked you to remember?"
        } else {
            txtSubQuestion.visibility = View.GONE
            editAnswer.visibility = View.GONE
        }

        editAnswer.setOnEditorActionListener { _, actionId, event ->
            if (actionId == KeyEvent.ACTION_DOWN || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                btnNext.performClick()
                true
            } else {
                false
            }
        }

        startElapsedTimeTimer()

        btnNext.setOnClickListener {
            (activity as QuestionSession).nextQuestion()
        }

        return view
    }

    private fun updateProgressBar() {
        val totalQuestions = allQuestionAmount ?: 0
        val currentIndex = currentQuestionIndex ?: 0
        if (totalQuestions > 0) {
            val progress = (currentIndex.toFloat() / totalQuestions.toFloat() * 100).toInt()
            progressBar.progress = progress
        } else {
            progressBar.progress = 0
        }
    }

    private fun startElapsedTimeTimer() {
        elapsedTime = 0
        handler.post(timerRunnable)
    }

    private fun resetElapsedTimeTimer() {
        handler.removeCallbacks(timerRunnable)
        elapsedTime = 0
        txtTimer.text = "Time: 00:00:00"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }

    fun getElapsedTime(): Long = elapsedTime

    fun getUserAnswer(): String? {
        return if (isRecallPhase) {
            editAnswer.text?.toString()?.trim().takeIf { it?.isNotEmpty() ?: false }
        } else {
            null
        }
    }

    companion object {
        fun newInstance(
            question: Question,
            difficultyLevel: Int,
            currentQuestionIndex: Int,
            allQuestionAmount: Int,
            isRecallPhase: Boolean
        ) = MemoryRecallFragment().apply {
            this.question = question
            this.difficultyLevel = difficultyLevel
            this.currentQuestionIndex = currentQuestionIndex + 1
            this.allQuestionAmount = allQuestionAmount
            this.isRecallPhase = isRecallPhase
        }
    }
}