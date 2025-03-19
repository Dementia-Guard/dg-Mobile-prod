package com.app.dementiaguard.Fragment.QuestionSession

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Activity.QuestionSession
import com.app.dementiaguard.Model.Question
import com.app.dementiaguard.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class BackwardFragment : Fragment() {

    private lateinit var question: Question
    private var difficultyLevel: Int? = 0
    private var currentQuestionIndex: Int? = 0
    private var allQuestionAmount: Int? = 0
    private lateinit var txtTimer: TextView

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
        val view = inflater.inflate(R.layout.fragment_backward, container, false)

        val txtQuestionCount = view.findViewById<TextView>(R.id.txtQuestionCount)
        val txtQuestionAmount = view.findViewById<TextView>(R.id.TxtQuestionAmount)
        val txtQuestion = view.findViewById<TextView>(R.id.txtQuestion)
        val layoutInputs = view.findViewById<LinearLayout>(R.id.layoutInputs)
        val btnNext = view.findViewById<Button>(R.id.btnBackwardNext)
        txtTimer = view.findViewById(R.id.txtTimer)

        txtQuestionCount.text = "Questions " + currentQuestionIndex.toString() + " /"
        txtQuestionAmount.text = allQuestionAmount.toString()

        txtQuestion.text = question.question

        question.possible_answers?.forEachIndexed { index, _ ->
            val textInputLayout = TextInputLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16) // Bottom margin
                }

                id = View.generateViewId() // Generate unique ID
                //hint = "Enter your answer ${index + 1}" // Set hint
                boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.secondary) // Stroke color
                hintTextColor = ContextCompat.getColorStateList(requireContext(), R.color.black) // Hint color
            }

            val editText = TextInputEditText(requireContext()).apply {
                id = View.generateViewId() // Generate unique ID
                hint = "Enter answer ${index + 1}" // Optional hint
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Text color
                textSize = 16f
            }

            textInputLayout.addView(editText) // Add EditText inside TextInputLayout
            layoutInputs.addView(textInputLayout) // Add TextInputLayout to parent

            // Refresh layout
            layoutInputs.invalidate()
            layoutInputs.requestLayout()
            layoutInputs.visibility = View.GONE

        }




        startElapsedTimeTimer()

        btnNext.setOnClickListener {
            (activity as QuestionSession).nextQuestion()
        }

        return view
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
        fun newInstance(question: Question, difficultyLevel: Int, currentQuestionIndex: Int, allQuestionAmount: Int) = BackwardFragment().apply {
            this.question = question
            this.difficultyLevel = difficultyLevel
            this.currentQuestionIndex = currentQuestionIndex+1
            this.allQuestionAmount =allQuestionAmount
        }
    }
}
