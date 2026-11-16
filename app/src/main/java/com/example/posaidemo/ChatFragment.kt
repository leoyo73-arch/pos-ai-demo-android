package com.example.posaidemo

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posaidemo.databinding.FragmentChatBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

/** 상담 탭과 비서 탭이 함께 쓰는 채팅 화면 */
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val adapter = ChatAdapter(onRetry = { viewModel.retry(it) })
    private var approvalDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etMessage.hint = viewModel.mode.hint
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvMessages.adapter = adapter

        // 예시 질문 칩: 누르면 바로 전송
        viewModel.mode.examples.forEach { example ->
            binding.chipExamples.addView(Chip(requireContext()).apply {
                text = example
                setOnClickListener { viewModel.send(example) }
            })
        }

        binding.btnSend.setOnClickListener { sendMessage() }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
        binding.etMessage.doAfterTextChanged { updateSendButton() }

        viewModel.messages.observe(viewLifecycleOwner) { list ->
            binding.chipExamples.isVisible = list.none { it.isUser }   // 첫 질문을 보내면 칩 숨김
            adapter.submitList(list) { binding.rvMessages.scrollToPosition(list.size - 1) }
        }
        viewModel.loading.observe(viewLifecycleOwner) { updateSendButton() }
        if (viewModel.mode == ChatMode.AGENT) {   // 승인 대기는 비서 탭에만 있음 (상담 탭이 배지를 0으로 덮어쓰지 않게)
            viewModel.pending.observe(viewLifecycleOwner) { list ->
                (activity as? MainActivity)?.updateAgentBadge(list.size)
                if (approvalDialog?.isShowing != true) list.firstOrNull()?.let(::showApproval)
            }
        }
    }

    /** 입력이 비어 있거나 응답을 기다리는 중이면 보내기 버튼을 끈다. */
    private fun updateSendButton() {
        val hasText = !binding.etMessage.text.isNullOrBlank()
        binding.btnSend.isEnabled = hasText && viewModel.loading.value != true
    }

    /** 승인 확인 창: 무엇을 승인하는지 거래 정보를 모두 보여 주고, 사장님이 직접 결정한다. */
    private fun showApproval(action: PendingAction) {
        val won = NumberFormat.getNumberInstance(Locale.KOREA).format(action.amount)
        val message = buildString {
            appendLine("거래번호: ${action.transactionId}")
            appendLine("결제 시각: ${action.paidAt}")
            appendLine("금액: ${won}원")
            appendLine("주문: ${action.items}")
            appendLine()
            append(if (action.isCancel) "사유: " else "보낼 문구:\n")
            append(action.detail.orEmpty())
        }
        approvalDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (action.isCancel) R.string.approve_cancel_title else R.string.approve_notice_title)
            .setMessage(message)
            .setCancelable(false)                       // 반드시 승인 또는 거절을 고르게
            .setPositiveButton(R.string.approve) { _, _ -> viewModel.decide(action, approve = true) }
            .setNegativeButton(R.string.reject) { _, _ -> viewModel.decide(action, approve = false) }
            // 버튼을 누르는 순간엔 창이 아직 닫히는 중이라, 다 닫힌 뒤에 다음 대기 건을 띄운다
            .setOnDismissListener {
                approvalDialog = null
                viewModel.pending.value?.firstOrNull()?.let(::showApproval)
            }
            .show()
            .also { dialog ->
                // 결제 취소는 되돌릴 수 없는 작업이라 승인 버튼을 빨간색으로
                if (action.isCancel) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(requireContext(), R.color.danger))
                }
            }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return
        viewModel.send(text)
        binding.etMessage.text.clear()
    }

    override fun onDestroyView() {
        // 화면 회전 시 창이 새는 것 방지 (대기 목록은 ViewModel에 남아 있어 화면이 다시 만들어지면 다시 뜸)
        approvalDialog?.setOnDismissListener(null)
        approvalDialog?.dismiss()
        approvalDialog = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(mode: ChatMode) = ChatFragment().apply {
            arguments = bundleOf(ChatViewModel.ARG_MODE to mode.name)
        }
    }
}
