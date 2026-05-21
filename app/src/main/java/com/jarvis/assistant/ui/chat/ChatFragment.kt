package com.jarvis.assistant.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jarvis.assistant.R
import com.jarvis.assistant.ai.AiResult
import com.jarvis.assistant.databinding.FragmentChatBinding
import com.jarvis.assistant.data.local.entity.ChatSessionEntity
import com.jarvis.assistant.data.repository.ChatRepository
import com.jarvis.assistant.utils.Constants
import com.jarvis.assistant.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    @Inject
    lateinit var chatRepository: ChatRepository

    @Inject
    lateinit var prefs: PreferencesManager

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private var currentSessionId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        createOrLoadSession()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.etMessage.text?.clear()
            }
        }

        binding.btnNewChat.setOnClickListener {
            lifecycleScope.launch {
                createNewSessionId()
            }
        }
    }

    private fun createOrLoadSession() {
        lifecycleScope.launch {
            val sessions = mutableListOf<ChatSessionEntity>()
            chatRepository.getAllSessions().collectLatest {
                sessions.clear()
                sessions.addAll(it)
                if (sessions.isEmpty()) {
                    createNewSessionId()
                } else {
                    loadSession(sessions.first().id)
                }
            }
        }
    }

    private suspend fun createNewSessionId(): Long {
        val id = chatRepository.createSession(
            provider = prefs.getString(Constants.PREF_ACTIVE_PROVIDER, "openai")
        )
        loadSession(id)
        return id
    }

    private fun loadSession(sessionId: Long) {
        currentSessionId = sessionId
        lifecycleScope.launch {
            chatRepository.getMessages(sessionId).collectLatest { messages ->
                chatAdapter.submitList(messages)
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }
    }

    private fun sendMessage(text: String) {
        val sessionId = currentSessionId
        if (sessionId == null) {
            lifecycleScope.launch {
                val id = createNewSessionId()
                sendMessageToSession(id, text)
            }
            return
        }
        sendMessageToSession(sessionId, text)
    }

    private fun sendMessageToSession(sessionId: Long, text: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false

        lifecycleScope.launch {
            val result = chatRepository.sendMessage(sessionId, text)

            binding.progressBar.visibility = View.GONE
            binding.btnSend.isEnabled = true

            if (result is AiResult.Error) {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    result.message,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
            }
            binding.rvMessages.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
