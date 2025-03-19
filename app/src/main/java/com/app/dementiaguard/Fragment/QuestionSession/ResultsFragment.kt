package com.app.dementiaguard.Fragment.QuestionSession

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Model.EvaluationResponse
import com.app.dementiaguard.Model.QuestionEvaluation
import com.app.dementiaguard.R

class ResultsFragment : Fragment() {

    private lateinit var timeSpentList: List<Long>
    private lateinit var evaluationResponse: EvaluationResponse

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)

        val txtScore = view.findViewById<TextView>(R.id.txtScore)
        val txtResults = view.findViewById<TextView>(R.id.txtResults)
        val txtTime = view.findViewById<TextView>(R.id.txtTime)
        val btnFinish = view.findViewById<Button>(R.id.btnFinish)
        val linearLayoutQuestionNum = view.findViewById<LinearLayout>(R.id.linearLayoutQuestionNum)
        val linearLayoutQuestionEval = view.findViewById<LinearLayout>(R.id.linearLayoutQuestionEval)

        // Extract data from evaluation response
        val sessionId = evaluationResponse.sessionId as? String ?: "Unknown"
        val userId = evaluationResponse.userId as? Number ?: 0
        val difficultyLevel = evaluationResponse.difficultyLevel as? Number ?: 0
        val avgScore = evaluationResponse.avgScore as? Double ?: 0.0
        val avgTime = evaluationResponse.avgTime as? Double ?: 0.0
        val evaluations = evaluationResponse.evaluations as? List<QuestionEvaluation> ?: emptyList()

        // Calculate total score (assuming avg_score is a percentage or normalized value)
        val totalQuestions = evaluations.size
        val correctCount = evaluations.count { it.correct }

        // Set score
        txtScore.text = "$correctCount out of $totalQuestions"

        // Build time results
        val resultsText = StringBuilder("Session Results:\n\n")
        var fullTime = 0L
        timeSpentList.forEachIndexed { index, time ->
            val hours = time / 3600
            val minutes = (time % 3600) / 60
            val seconds = time % 60
            val timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            resultsText.append("Question ${index + 1}: $timeFormatted\n")
            fullTime += time
        }
        txtResults.text = "Session Results"
        txtResults.setTypeface(null, Typeface.BOLD)
        txtResults.textSize = 32f
        txtTime.text = "Avg Time: $avgTime seconds\nTotal Time: $fullTime ms"

        // Populate question numbers and evaluations (using dummy logic for now since evaluations are ignored)
        evaluations.forEachIndexed { index, evaluation ->
            val isCorrect = evaluation.correct // Use actual correctness from evaluation

            val questionTextView = TextView(context).apply {
                text = "Question ${index + 1}"
                textSize = 24f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(context, if (isCorrect) R.color.bg_success else R.color.bg_danger))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 48, 16)
                }
            }
            linearLayoutQuestionNum.addView(questionTextView)

            val evalImageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(32.dpToPx(), 32.dpToPx()).apply {
                    setMargins(0, 0, 0, 16)
                }
                setImageResource(if (isCorrect) R.drawable.check else R.drawable.cross)
            }
            linearLayoutQuestionEval.addView(evalImageView)
        }

        // Finish button
        btnFinish.setOnClickListener {
            activity?.finish()
        }

        return view
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    companion object {
        fun newInstance(timeSpentList: List<Long>, evaluationResponse: EvaluationResponse) = ResultsFragment().apply {
            this.timeSpentList = timeSpentList
            this.evaluationResponse = evaluationResponse
        }
    }
}