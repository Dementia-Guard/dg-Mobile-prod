package com.app.dementiaguard.Fragment.QuestionSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.dementiaguard.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LoadingFragment : Fragment() {

    private lateinit var loading: TextView
    private var loadingJob: Job? = null  // To manage the coroutine

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loading, container, false)

        loading = view.findViewById(R.id.txtLoading)

        // Start the loading text cycle
        startLoadingTextCycle()

        return view
    }

    private fun startLoadingTextCycle() {
        // Define the text array to cycle through
        val loadingTexts = arrayOf(
            "Your Questioning Session\nis being generated.",
            "Please wait...",
            "This can be take some time."
        )

        // Launch a coroutine in the fragment's lifecycle scope
        loadingJob = lifecycleScope.launch {
            var index = 0
            while (isActive) {  // Runs until the coroutine is cancelled
                // Fade out current text
                loading.animate()
                    .alpha(0f)  // Fade to fully transparent
                    .setDuration(500L)  // 0.5 seconds for fade-out
                    .withEndAction {
                        // Update text after fade-out completes
                        loading.text = loadingTexts[index]
                        // Fade in new text
                        loading.animate()
                            .alpha(1f)  // Fade to fully opaque
                            .setDuration(500L)  // 0.5 seconds for fade-in
                            .start()
                    }
                    .start()

                index = (index + 1) % loadingTexts.size  // Cycle through the array
                delay(4000L)  // Wait 4 seconds between full cycles
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel the coroutine when the view is destroyed
        loadingJob?.cancel()
    }

    companion object {
        fun newInstance() = LoadingFragment()
    }
}