package com.app.dementiaguard.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dementiaguard.Model.PreviousSession
import com.app.dementiaguard.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentActivitiesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PreviousSessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recent_activities, container, false)

        recyclerView = view.findViewById(R.id.rv_previous_sessions)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Get previous sessions from arguments
        val previousSessionsJson = arguments?.getString("previous_sessions")
        val previousSessions: List<PreviousSession> = if (previousSessionsJson != null) {
            Gson().fromJson(previousSessionsJson, object : TypeToken<List<PreviousSession>>() {}.type)
        } else {
            emptyList()
        }

        // Set up RecyclerView adapter
        adapter = PreviousSessionsAdapter(previousSessions)
        recyclerView.adapter = adapter

        return view
    }
}

class PreviousSessionsAdapter(private val sessions: List<PreviousSession>) :
    RecyclerView.Adapter<PreviousSessionsAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sessionIdText: TextView = itemView.findViewById(R.id.tv_session_id)
        val currentDifficultyText: TextView = itemView.findViewById(R.id.tv_current_difficulty)
        val adjustedDifficultyText: TextView = itemView.findViewById(R.id.tv_adjusted_difficulty)
        val avgScoreText: TextView = itemView.findViewById(R.id.tv_avg_score)
        val avgResTimeText: TextView = itemView.findViewById(R.id.tv_avg_res_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_previous_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.sessionIdText.text = "#${session.sessionId}"
        holder.currentDifficultyText.text = session.currentDifficultyLevel.toString()
        holder.adjustedDifficultyText.text = session.adjustedDifficultyLevel.toString()
        holder.avgScoreText.text = "${session.avgScore * 100}%"
        holder.avgResTimeText.text = "${session.avgResTime}s"
    }

    override fun getItemCount(): Int = sessions.size
}