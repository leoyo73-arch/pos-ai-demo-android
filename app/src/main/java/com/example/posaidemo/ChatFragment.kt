package com.example.posaidemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.posaidemo.databinding.FragmentChatBinding
import java.text.NumberFormat
import java.util.Locale

/** 상담 탭과 비서 탭이 함께 쓰는 채팅 화면 */
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val adapter = ChatAdapter()
    private var approvalDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.etMessage.hint = viewModel.mode.hint
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvMessages.adapter = adapter

        binding.btnSend.setOnClickListener { sendMessage() }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        viewModel.messages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list) { binding.rvMessages.scrollToPosition(list.size - 1) }
        }
        viewModel.loading.observe(viewLifecycleOwner) { binding.btnSend.isEnabled = !it }
        viewModel.pending.observe(viewLifecycleOwner) { list ->
            if (approvalDialog?.isShowing != true) list.firstOrNull()?.let(::showApproval)
        }
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
        approvalDialog = AlertDialog.Builder(requireContext())
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
