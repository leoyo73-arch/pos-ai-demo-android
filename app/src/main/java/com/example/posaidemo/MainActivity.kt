package com.example.posaidemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.posaidemo.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tabs = listOf(ChatMode.CONSULT, ChatMode.AGENT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int): Fragment = ChatFragment.newInstance(tabs[position])
        }
        binding.pager.isUserInputEnabled = false   // 채팅 중 실수로 옆으로 밀려 탭이 바뀌지 않게
        binding.pager.offscreenPageLimit = 1       // 탭을 오가도 대화가 유지되게

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = getString(if (tabs[position] == ChatMode.CONSULT) R.string.tab_consult else R.string.tab_agent)
        }.attach()
    }

    /** 비서 탭에 승인 대기 건수 배지를 표시. 상담 탭에 있어도 승인할 일이 있다는 걸 알 수 있다. */
    fun updateAgentBadge(count: Int) {
        val tab = binding.tabs.getTabAt(tabs.indexOf(ChatMode.AGENT)) ?: return
        if (count > 0) {
            tab.orCreateBadge.apply {
                number = count
                backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.danger)
            }
        } else {
            tab.removeBadge()
        }
    }
}
