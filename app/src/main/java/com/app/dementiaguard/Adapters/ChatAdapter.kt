package com.app.dementiaguard.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.dementiaguard.R
import com.app.dementiaguard.models.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(if (viewType == 0) R.layout.item_chat_assistant else R.layout.item_chat_patient, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text

        if (message.imageRes != null) {
            holder.imageView?.visibility = View.VISIBLE
            holder.imageView?.setImageResource(message.imageRes)
        } else {
            holder.imageView?.visibility = View.GONE
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 1 else 0
    }

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.message_text)
        val imageView: ImageView? = view.findViewById(R.id.image_view)
    }
}