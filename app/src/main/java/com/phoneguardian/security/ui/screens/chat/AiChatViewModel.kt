package com.phoneguardian.security.ui.screens.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.phoneguardian.security.PhoneGuardianApp
import com.phoneguardian.security.domain.model.ChatMessage
import com.phoneguardian.security.domain.model.MessageSender
import com.phoneguardian.security.domain.model.SecurityFinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PhoneGuardianApp
    private val geminiClient = app.geminiApiClient

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AGENT_AI,
                text = "مرحباً! أنا المستشار الأمني الذكي لحارس الهاتف (Phone Guardian AI). كيف يمكنني مساعدتك في تأمين جهازك، تحليل التطبيقات، أو توضيح أي ثغرة مكتشفة؟"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = userText)
        _messages.value = _messages.value + userMsg

        _isLoading.value = true
        viewModelScope.launch {
            val historyPairs = _messages.value
                .dropLast(1)
                .map { (if (it.sender == MessageSender.USER) "USER" else "MODEL") to it.text }

            val result = geminiClient.chatWithAi(historyPairs, userText)
            val replyText = result.getOrElse { error ->
                "تعذر الاتصال بـ Gemini API: ${error.localizedMessage} (يرجى التأكد من إضافة مفتاح API في شاشة الإعدادات)."
            }

            val aiMsg = ChatMessage(sender = MessageSender.AGENT_AI, text = replyText)
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }

    fun initiateChatForFinding(finding: SecurityFinding) {
        val query = "أرجو تحليل التهديد التالي أمنياً وتقديم خطوات تفصيلية لحله: [${finding.title}] في التطبيق أو الإعداد (${finding.packageName ?: "نظام أندرويد"}). وصف الخطر: ${finding.description}"
        sendMessage(query)
    }
}
