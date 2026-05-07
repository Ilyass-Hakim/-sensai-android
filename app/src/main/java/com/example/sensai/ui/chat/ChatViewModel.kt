package com.example.sensai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensai.data.local.TokenManager
import com.example.sensai.data.network.BackendApi
import com.example.sensai.data.network.ChatWebSocketClient
import com.example.sensai.data.network.dto.chat.ChatRoom
import com.example.sensai.data.network.dto.chat.ChatWSMessage
import com.example.sensai.data.network.dto.chat.ChatWSReaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val backendApi: BackendApi,
    private val webSocketClient: ChatWebSocketClient,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val rooms: StateFlow<List<ChatRoom>> = _rooms.asStateFlow()

    private val _currentRoom = MutableStateFlow<ChatRoom?>(null)
    val currentRoom: StateFlow<ChatRoom?> = _currentRoom.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatWSMessage>>(emptyList())
    val messages: StateFlow<List<ChatWSMessage>> = _messages.asStateFlow()

    private val _reactions = MutableStateFlow<List<ChatWSReaction>>(emptyList())
    val reactions: StateFlow<List<ChatWSReaction>> = _reactions.asStateFlow()

    private val _currentUserId = MutableStateFlow<Long>(-1)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    private var filterAnimeId: Int? = null

    init {
        fetchRooms()
        viewModelScope.launch {
            tokenManager.userId.collect { id ->
                if (id != null) _currentUserId.value = id
            }
        }
        viewModelScope.launch {
            // Initial load
            _currentUserId.value = tokenManager.userId.filterNotNull().first()
            
            launch {
                webSocketClient.messages.collect { newMessage ->
                    if (newMessage.roomId == _currentRoom.value?.id) {
                        // Deduplicate: remove any optimistic message with same content from same user
                        val filtered = _messages.value.filterNot { 
                            it.id < 0 && it.content == newMessage.content && it.userId == newMessage.userId 
                        }
                        _messages.value = (listOf(newMessage) + filtered).take(100)
                    }
                }
            }

            launch {
                webSocketClient.reactions.collect { reaction ->
                    _reactions.value = _reactions.value + reaction
                }
            }
        }
    }

    private fun fetchRooms() {
        viewModelScope.launch {
            try {
                val allRooms = backendApi.getChatRooms()
                android.util.Log.d("ChatDebug", "Total rooms in DB: ${allRooms.size}")
                
                val filtered = if (filterAnimeId != null) {
                    allRooms.filter { it.animeId == filterAnimeId }
                } else {
                    allRooms.filter { it.animeId == null }
                }
                
                android.util.Log.d("ChatDebug", "Filtered rooms for anime $filterAnimeId: ${filtered.size}")
                _rooms.value = filtered
            } catch (e: Exception) {
                android.util.Log.e("ChatDebug", "Error fetching rooms: ${e.message}", e)
            }
        }
    }

    fun selectRoom(room: ChatRoom) {
        // If we are already in this room, don't reset anything!
        if (_currentRoom.value?.id == room.id) return
        
        viewModelScope.launch {
            _currentRoom.value = room
            _messages.value = emptyList()
            _reactions.value = emptyList()
            
            // Ensure userId is loaded before connecting
            if (_currentUserId.value == -1L) {
                _currentUserId.value = tokenManager.userId.filterNotNull().first()
            }

            // Fetch history via REST
            try {
                val history = backendApi.getRoomHistory(room.id)
                android.util.Log.d("ChatViewModel", "REST History received: ${history.size} messages")
                // Sort newest first for reverseLayout LazyColumn
                _messages.value = history.sortedByDescending { it.id }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Failed to fetch history via REST: ${e.message}", e)
            }
            
            webSocketClient.connect("ws://10.0.2.2:8081/ws")
            webSocketClient.subscribeToRoom(room.id, this)
            
            // Give STOMP more time to acknowledge subscriptions before joining
            kotlinx.coroutines.delay(1000)
            
            webSocketClient.joinRoom(room.id, _currentUserId.value)
        }
    }

    fun sendMessage(content: String) {
        val room = _currentRoom.value ?: return
        if (content.isBlank()) return
        
        // Optimistic update: add message locally immediately
        val optimisticMessage = ChatWSMessage(
            id = -System.currentTimeMillis(), // Temporary negative ID
            roomId = room.id,
            userId = _currentUserId.value,
            username = "Moi", // Will be updated when server responds
            content = content,
            createdAt = java.time.LocalDateTime.now().toString()
        )
        _messages.value = (listOf(optimisticMessage) + _messages.value).take(100)

        viewModelScope.launch {
            webSocketClient.sendMessage(room.id, _currentUserId.value, content)
        }
    }

    fun reactToMessage(messageId: Long, emoji: String) {
        val room = _currentRoom.value ?: return
        viewModelScope.launch {
            webSocketClient.reactToMessage(messageId, emoji, room.id, _currentUserId.value)
        }
    }

    fun selectRoomByAnime(animeId: Int, animeName: String) {
        filterAnimeId = animeId
        viewModelScope.launch {
            try {
                // This call ensures the 3 rooms exist on the server
                backendApi.getOrCreateAnimeRooms(animeId, animeName)
                // Now fetch and filter
                fetchRooms()
                _currentRoom.value = null 
                _messages.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketClient.disconnect()
    }
}
