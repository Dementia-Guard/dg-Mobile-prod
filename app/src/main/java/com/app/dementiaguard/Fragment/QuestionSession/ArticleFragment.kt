package com.app.dementiaguard.Fragment.QuestionSession

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Activity.QuestionSession
import com.app.dementiaguard.Model.Question
import com.app.dementiaguard.R
import com.google.android.material.textfield.TextInputEditText

class ArticleFragment : Fragment() {

    private lateinit var question: Question
    private var difficultyLevel: Int? = 0
    private var currentQuestionIndex: Int? = 0
    private var allQuestionAmount: Int? = 0
    private lateinit var txtTimer: TextView
    private lateinit var editAnswer: TextInputEditText
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_article, container, false)
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val txtBody = view.findViewById<TextView>(R.id.txtBody)
        val btnFinishReading = view.findViewById<Button>(R.id.btnFinishReading)
        val layoutSubQuestion = view.findViewById<LinearLayout>(R.id.layoutSubQuestion)
        val txtSubQuestion = view.findViewById<TextView>(R.id.txtSubQuestion)
        editAnswer = view.findViewById<TextInputEditText>(R.id.editAnswer)
        btnNext = view.findViewById<Button>(R.id.btnArticleNext)
        val txtQuestionCount = view.findViewById<TextView>(R.id.txtQuestionCount)
        val txtQuestionAmount = view.findViewById<TextView>(R.id.TxtQuestionAmount)
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        txtTimer = view.findViewById(R.id.txtTimer)


        txtQuestionCount.text = "Question $currentQuestionIndex "
        txtQuestionAmount.text = "out of $allQuestionAmount"

        txtTitle.text = question.title
        txtBody.text = question.article
        layoutSubQuestion.visibility = View.GONE

        updateProgressBar()

        startElapsedTimeTimer()

        btnFinishReading.setOnClickListener {
            btnFinishReading.visibility = View.GONE
            txtTitle.visibility = View.GONE
            txtBody.visibility = View.GONE
            btnNext.visibility = View.VISIBLE
            layoutSubQuestion.visibility = View.VISIBLE
            txtSubQuestion.text = question.question
        }

        editAnswer.setOnEditorActionListener { _, actionId, event ->
            if (actionId == KeyEvent.ACTION_DOWN || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                btnNext.performClick()  // Trigger the Next button click
                true
            } else {
                false
            }
        }

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
        elapsedTime = 0 // Reset time to 0
        handler.post(timerRunnable) // Start the timer
    }

    private fun resetElapsedTimeTimer() {
        handler.removeCallbacks(timerRunnable) // Stop the timer
        elapsedTime = 0 // Reset time
        txtTimer.text = "Time: 00:00:00"  // Update UI
    }


    override fun onDestroyView() {
        //super.onDestroyView()
        //countDownTimer?.cancel()

        super.onDestroyView()
        handler.removeCallbacks(timerRunnable)
    }

    fun getElapsedTime(): Long {
        return elapsedTime
    }

    fun getUserAnswer(): String? {
        val radioGroup = view?.findViewById<RadioGroup>(R.id.radioGroup)
        val editAnswer = view?.findViewById<TextInputEditText>(R.id.editAnswer)

        return editAnswer?.text?.toString()?.trim().takeIf { it?.isNotEmpty() ?: false }
    }

    companion object {
        fun newInstance(question: Question, difficultyLevel: Int, currentQuestionIndex: Int, allQuestionAmount: Int) = ArticleFragment().apply {
            this.question = question
            this.difficultyLevel = difficultyLevel
            this.currentQuestionIndex = currentQuestionIndex+1
            this.allQuestionAmount =allQuestionAmount
        }
    }
}
