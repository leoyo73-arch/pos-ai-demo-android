package com.example.posaidemo

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.posaidemo.databinding.ItemMessageBinding

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    class VH(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = getItem(position)
        val b = holder.binding
        val context = b.root.context

        b.tvBubble.text = msg.text
        b.tvBubble.setBackgroundResource(
            if (msg.isUser) R.drawable.bg_bubble_user else R.drawable.bg_bubble_ai
        )
        b.tvBubble.setTextColor(
            ContextCompat.getColor(context, if (msg.isUser) R.color.bubble_user_text else R.color.bubble_ai_text)
        )
        b.tvBubble.alpha = if (msg.isLoading) 0.6f else 1f

        // 근거: AI 답변이고 근거가 있을 때만 표시
        if (!msg.isUser && msg.sources.isNotEmpty()) {
            b.tvSources.text = "참고한 매뉴얼\n" + msg.sources.joinToString("\n") { "· $it" }
            b.tvSources.visibility = View.VISIBLE
        } else {
            b.tvSources.visibility = View.GONE
        }

        // 정렬은 말풍선+근거 묶음(container) 단위로
        val params = b.container.layoutParams as FrameLayout.LayoutParams
        params.gravity = if (msg.isUser) Gravity.END else Gravity.START
        b.container.layoutParams = params
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
            override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
        }
    }
}
