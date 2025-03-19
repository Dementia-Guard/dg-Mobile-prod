package com.app.dementiaguard.Fragment.QuestionSession

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Activity.QuestionSession
import com.app.dementiaguard.Model.Question
import com.app.dementiaguard.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import kotlin.random.Random

class ObjectRecallFragment : Fragment() {

    private lateinit var question: Question
    private var difficultyLevel: Int? = 0
    private var currentQuestionIndex: Int? = 0
    private var allQuestionAmount: Int? = 0
    private lateinit var txtTimer: TextView
    private lateinit var objectRecallImage: ImageView
    private lateinit var imageLoadingProgress: ProgressBar

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
        val view = inflater.inflate(R.layout.fragment_object_recall, container, false)

        val txtQuestionCount = view.findViewById<TextView>(R.id.txtQuestionCount)
        val txtQuestionAmount = view.findViewById<TextView>(R.id.TxtQuestionAmount)
        val txtQuestion = view.findViewById<TextView>(R.id.txtQuestion)
        val btnNext = view.findViewById<Button>(R.id.btnObjectRecallNext)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val editAnswer = view.findViewById<TextInputEditText>(R.id.editAnswer)
        val editAnswerLayout = view.findViewById<TextInputLayout>(R.id.editAnswerLayout)
        txtTimer = view.findViewById(R.id.txtTimer)
        objectRecallImage = view.findViewById(R.id.object_recall_image)
        imageLoadingProgress = view.findViewById(R.id.imageLoadingProgress)

        txtQuestionCount.text = "Questions $currentQuestionIndex /"
        txtQuestionAmount.text = allQuestionAmount.toString()

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
                    gravity = Gravity.CENTER
                    setPadding(16, 24, 16, 24) // Padding in dp
                    textSize = 24f // Text size in sp
                }
                radioGroup.addView(radioButton)
            }
        } else {
            txtQuestion.text = question.link_of_img
            radioGroup.visibility = View.GONE
            editAnswer.visibility = View.VISIBLE
            editAnswerLayout.visibility = View.VISIBLE
        }

        // Load image from Pixabay API if link_of_img exists
        question.link_of_img?.let { url ->
            loadRandomImageFromPixabay(url)
        } ?: run {
            // Fallback to default image if no link is provided
            imageLoadingProgress.visibility = View.GONE
            Glide.with(this)
                .load(R.drawable.home_logo)
                .into(objectRecallImage)
        }

        startElapsedTimeTimer()

        btnNext.setOnClickListener {
            (activity as QuestionSession).nextQuestion()
        }

        return view
    }

    private fun loadRandomImageFromPixabay(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    imageLoadingProgress.visibility = View.VISIBLE
                    objectRecallImage.visibility = View.INVISIBLE // Optionally hide image while loading
                }
                // Fetch JSON from the Pixabay API
                val response = URL(url).readText()
                val jsonObject = JSONObject(response)
                val hitsArray = jsonObject.getJSONArray("hits")

                // Select a random image URL from the hits array
                val randomIndex = Random.nextInt(hitsArray.length())
                val randomHit = hitsArray.getJSONObject(randomIndex)
                val imageUrl = randomHit.getString("webformatURL") // or "largeImageURL" for higher quality

                // Load the image into ImageView on the main thread
                withContext(Dispatchers.Main) {
                    Glide.with(this@ObjectRecallFragment)
                        .load(imageUrl)
                        .placeholder(R.drawable.home_logo) // Fallback image while loading
                        .error(R.drawable.home_logo) // Fallback image on error
                        .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                imageLoadingProgress.visibility = View.GONE
                                objectRecallImage.visibility = View.VISIBLE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                imageLoadingProgress.visibility = View.GONE
                                objectRecallImage.visibility = View.VISIBLE
                                return false
                            }
                        })
                        .into(objectRecallImage)
                }
            } catch (e: Exception) {
                // Handle errors (e.g., network failure, JSON parsing error)
                withContext(Dispatchers.Main) {
                    imageLoadingProgress.visibility = View.GONE
                    objectRecallImage.visibility = View.VISIBLE
                    Glide.with(this@ObjectRecallFragment)
                        .load(R.drawable.home_logo)
                        .into(objectRecallImage)
                }
            }
        }
    }

    private fun startElapsedTimeTimer() {
        elapsedTime = 0 // Reset time to 0
        handler.post(timerRunnable) // Start the timer
    }

    private fun resetElapsedTimeTimer() {
        handler.removeCallbacks(timerRunnable) // Stop the timer
        elapsedTime = 0 // Reset time
        txtTimer.text = "Time: 00:00:00" // Update UI
    }

    override fun onDestroyView() {
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
        fun newInstance(
            question: Question,
            difficultyLevel: Int,
            currentQuestionIndex: Int,
            allQuestionAmount: Int
        ) = ObjectRecallFragment().apply {
            this.question = question
            this.difficultyLevel = difficultyLevel
            this.currentQuestionIndex = currentQuestionIndex + 1
            this.allQuestionAmount = allQuestionAmount
        }
    }
}