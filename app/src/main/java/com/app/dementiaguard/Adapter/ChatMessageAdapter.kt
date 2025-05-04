package com.app.dementiaguard.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.dementiaguard.R
import com.app.dementiaguard.Model.ChatMessage

class ChatMessageAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_ASSISTANT = 2
    
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_ASSISTANT
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_assistant, parent, false)
            AssistantMessageViewHolder(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        if (holder is UserMessageViewHolder) {
            holder.bind(message)
        } else if (holder is AssistantMessageViewHolder) {
            holder.bind(message)
        }
    }
    
    override fun getItemCount(): Int = messages.size
    
    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvUserMessage)
        
        fun bind(message: ChatMessage) {
            messageText.text = message.message
        }
    }
    
    inner class AssistantMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvAssistantMessage)
        private val messageImage: ImageView = itemView.findViewById(R.id.ivQuizImage)
        
        fun bind(message: ChatMessage) {
            messageText.text = message.message
            
            if (message.image != null) {
                messageImage.setImageBitmap(message.image)
                messageImage.isVisible = true
            } else {
                messageImage.isVisible = false
            }
        }
    }
}
