package com.app.dementiaguard.Fragment.QuestionSession

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.app.dementiaguard.Activity.QuestionSession
import com.app.dementiaguard.Model.Question
import com.app.dementiaguard.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.time.temporal.TemporalAmount

class ProblemSolvingFragment : Fragment() {

    private lateinit var question: Question
    private var difficultyLevel: Int? = 0
    private var currentQuestionIndex: Int? = 0
    private var allQuestionAmount: Int? = 0
    private lateinit var txtTimer: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var editAnswer: TextInputEditText
    private lateinit var btnNext: Button

    private var elapsedTime = 0L
    private val handler = Handler(Looper.getMainLooper())

    //private var countDownTimer: CountDownTimer? = null
    //private val timeLimitMillis: Long = 30000 // 30 seconds

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
        val view = inflater.inflate(R.layout.fragment_day_today, container, false)

        val txtQuestionCount = view.findViewById<TextView>(R.id.txtQuestionCount)
        val txtQuestionAmount = view.findViewById<TextView>(R.id.TxtQuestionAmount)
        val txtQuestion = view.findViewById<TextView>(R.id.txtQuestion)
        btnNext = view.findViewById<Button>(R.id.btnDayTodayNext)
        editAnswer = view.findViewById<TextInputEditText>(R.id.editAnswer)
        val editAnswerLayout = view.findViewById<TextInputLayout>(R.id.editAnswerLayout)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        txtTimer = view.findViewById(R.id.txtTimer)
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        txtQuestionCount.text = "Question $currentQuestionIndex "
        txtQuestionAmount.text = "out of $allQuestionAmount"

        updateProgressBar()

        if (difficultyLevel == 0) {
            txtQuestion.text = question.question + " (Select an answer)"
            editAnswer.visibility = View.GONE
            radioGroup.visibility = View.VISIBLE

            radioGroup.removeAllViews()

            question.possible_answers?.forEach { answer ->
                val radioButton = RadioButton(context).apply {
                    text = answer
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = (8 * resources.displayMetrics.density).toInt() // 8dp margin
                    }
                    background = resources.getDrawable(R.drawable.radio_button_background, null)
                    buttonDrawable = null
                    gravity = android.view.Gravity.CENTER
                    setPadding(16, 24, 16, 24) // Padding in dp
                    textSize = 24f // Text size in sp
                }
                radioGroup.addView(radioButton)
            }
        } else {
            txtQuestion.text = question.question + " (Type the answer)"
            radioGroup.visibility = View.GONE
            editAnswer.visibility = View.VISIBLE
            editAnswerLayout.visibility = View.VISIBLE
        }

        editAnswer.setOnEditorActionListener { _, actionId, event ->
            if (actionId == KeyEvent.ACTION_DOWN || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                btnNext.performClick()  // Trigger the Next button click
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

    /*private fun startCountDownTimer() {
        countDownTimer?.cancel() // Cancel any previous timer if exists

        countDownTimer = object : CountDownTimer(timeLimitMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtTimer.text = "Time: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                txtTimer.text = "Time's up!"
                (activity as QuestionSession).nextQuestion() // Auto move to next question
            }
        }.start()
    }*/

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

        return if (difficultyLevel == 0) {
            // For multiple-choice (radio buttons)
            val selectedRadioButtonId = radioGroup?.checkedRadioButtonId
            if (selectedRadioButtonId != null && selectedRadioButtonId != -1) {
                val selectedRadioButton = view?.findViewById<RadioButton>(selectedRadioButtonId)
                selectedRadioButton?.text?.toString()
            } else {
                null // No selection made
            }
        } else {
            // For text input
            editAnswer?.text?.toString()?.trim().takeIf { it?.isNotEmpty() ?: false }
        }
    }

    companion object {
        fun newInstance(question: Question, difficultyLevel: Int, currentQuestionIndex: Int, allQuestionAmount: Int) = ProblemSolvingFragment().apply {
            this.question = question
            this.difficultyLevel = difficultyLevel
            this.currentQuestionIndex = currentQuestionIndex+1
            this.allQuestionAmount =allQuestionAmount
        }
    }
}
