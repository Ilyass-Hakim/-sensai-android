package com.example.sensai.data.network

import com.example.sensai.data.network.dto.chat.ChatRoom
import com.example.sensai.data.network.dto.chat.ChatWSMessage
import com.example.sensai.data.network.dto.chat.ChatWSReaction
import com.example.sensai.data.network.dto.chat.ChatSendMessageRequest
import com.example.sensai.data.network.dto.chat.ChatJoinRequest
import com.google.gson.Gson
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatWebSocketClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private val stompClient = StompClient(OkHttpWebSocketClient(okHttpClient))
    private var session: StompSession? = null

    private val _messages = MutableSharedFlow<ChatWSMessage>(replay = 0)
    val messages: Flow<ChatWSMessage> = _messages.asSharedFlow()

    private val _history = MutableSharedFlow<List<ChatWSMessage>>(replay = 1)
    val history: Flow<List<ChatWSMessage>> = _history.asSharedFlow()

    private val _reactions = MutableSharedFlow<ChatWSReaction>(replay = 1)
    val reactions: Flow<ChatWSReaction> = _reactions.asSharedFlow()

    private var subscriptionJob: kotlinx.coroutines.Job? = null

    suspend fun connect(url: String) {
        if (session == null) {
            try {
                android.util.Log.d("ChatWS", "Connecting to $url")
                session = stompClient.connect(url)
                android.util.Log.d("ChatWS", "Connected!")
            } catch (e: Exception) {
                android.util.Log.e("ChatWS", "Connection failed: ${e.message}", e)
            }
        }
    }

    suspend fun subscribeToRoom(roomId: Long, scope: kotlinx.coroutines.CoroutineScope) {
        val s = session ?: run {
            android.util.Log.e("ChatWS", "Cannot subscribe: Session is null")
            return
        }
        
        // Cancel previous subscriptions for other rooms
        subscriptionJob?.cancel()
        
        android.util.Log.d("ChatWS", "Subscribing to room $roomId")
        
        subscriptionJob = scope.launch {
            // Subscribe to messages
            launch {
                s.subscribeText("/topic/rooms/$roomId").map { json ->
                    try {
                        gson.fromJson(json, ChatWSMessage::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("ChatWS", "Error parsing message JSON: $json", e)
                        null
                    }
                }.filterNotNull().collect { 
                    _messages.emit(it)
                }
            }

            // Subscribe to history
            launch {
                s.subscribeText("/topic/rooms/$roomId/history").collect { json ->
                    android.util.Log.d("ChatWS", "RAW HISTORY JSON RECEIVED: $json")
                    try {
                        val history = gson.fromJson(json, Array<ChatWSMessage>::class.java).toList()
                        android.util.Log.d("ChatWS", "Parsed history: ${history.size} messages")
                        _history.emit(history)
                    } catch (e: Exception) {
                        android.util.Log.e("ChatWS", "Error parsing history JSON: $json", e)
                    }
                }
            }

            // Subscribe to reactions
            launch {
                s.subscribeText("/topic/rooms/$roomId/reactions").map { 
                    gson.fromJson(it, ChatWSReaction::class.java)
                }.collect { 
                    _reactions.emit(it)
                }
            }
        }
    }

    suspend fun sendMessage(roomId: Long, userId: Long, content: String) {
        val s = session ?: run {
            android.util.Log.e("ChatWS", "Cannot send message: Not connected")
            return
        }
        val request = ChatSendMessageRequest(roomId, userId, content)
        val json = gson.toJson(request)
        android.util.Log.d("ChatWS", "Sending message: $json")
        try {
            s.sendText("/app/chat.send", json)
            android.util.Log.d("ChatWS", "Message sent successfully")
        } catch (e: Exception) {
            android.util.Log.e("ChatWS", "Failed to send message: ${e.message}", e)
        }
    }

    suspend fun joinRoom(roomId: Long, userId: Long) {
        val s = session ?: run {
            android.util.Log.e("ChatWS", "Cannot join room: Not connected")
            return
        }
        val request = ChatJoinRequest(roomId, userId)
        val json = gson.toJson(request)
        android.util.Log.d("ChatWS", "Joining room $roomId as user $userId")
        try {
            s.sendText("/app/chat.join", json)
            android.util.Log.d("ChatWS", "Join request sent successfully")
        } catch (e: Exception) {
            android.util.Log.e("ChatWS", "Failed to send join request: ${e.message}", e)
        }
    }

    suspend fun reactToMessage(messageId: Long, emoji: String, roomId: Long, userId: Long) {
        val s = session ?: return
        val reaction = ChatWSReaction(messageId, roomId, userId, "", emoji)
        val json = gson.toJson(reaction)
        try {
            s.sendText("/app/chat.react", json)
        } catch (e: Exception) {
            android.util.Log.e("ChatWS", "Failed to send reaction: ${e.message}")
        }
    }

    fun disconnect() {
        // Krossbow session doesn't have a direct disconnect, it's usually handled by the scope
        session = null
    }
}
