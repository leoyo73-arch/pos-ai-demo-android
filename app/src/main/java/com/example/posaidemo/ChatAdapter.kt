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
        val params = b.container.layoutParams as FrameLayout.LayoutParams

        b.tvBubble.text = msg.text
        b.tvBubble.alpha = if (msg.isLoading) 0.6f else 1f

        if (msg.isNotice) {
            // 승인·거절 결과: 가운데 정렬, 말풍선 없이 작은 회색 글씨
            b.tvBubble.background = null
            b.tvBubble.textSize = 13f
            b.tvBubble.setTextColor(ContextCompat.getColor(context, R.color.notice_text))
            params.gravity = Gravity.CENTER_HORIZONTAL
        } else {
            b.tvBubble.setBackgroundResource(
                if (msg.isUser) R.drawable.bg_bubble_user else R.drawable.bg_bubble_ai
            )
            b.tvBubble.textSize = 15f
            b.tvBubble.setTextColor(
                ContextCompat.getColor(context, if (msg.isUser) R.color.bubble_user_text else R.color.bubble_ai_text)
            )
            params.gravity = if (msg.isUser) Gravity.END else Gravity.START
        }
        b.container.layoutParams = params

        // 근거: AI 답변이고 근거가 있을 때만 표시. 뷰가 재사용되므로 else에서 꼭 숨긴다.
        if (!msg.isUser && !msg.isNotice && msg.sources.isNotEmpty()) {
            b.tvSources.text = context.getString(R.string.sources_title) + "\n" +
                msg.sources.joinToString("\n") { "· $it" }
            b.tvSources.visibility = View.VISIBLE
        } else {
            b.tvSources.visibility = View.GONE
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) = a.id == b.id
            override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) = a == b
        }
    }
}
