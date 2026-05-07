package com.example.sensai.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.data.network.dto.chat.ChatRoom
import com.example.sensai.data.network.dto.chat.ChatWSMessage
import com.example.sensai.ui.theme.*

@Composable
fun DiscussionScreen(
    initialAnimeId: Int = -1,
    initialAnimeName: String? = null,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val reactions by viewModel.reactions.collectAsState()
    
    // Always start at tab 0 (Salons) so user can choose from the 3 rooms
    var selectedTab by androidx.compose.runtime.saveable.rememberSaveable { 
        mutableIntStateOf(0) 
    }

    // When we get a new initialAnimeId, we definitely want to switch to the chat tab
    LaunchedEffect(initialAnimeId, initialAnimeName) {
        if (initialAnimeId != -1 && initialAnimeName != null) {
            android.util.Log.d("ChatUI", "Joining anime room: $initialAnimeName")
            viewModel.selectRoomByAnime(initialAnimeId, initialAnimeName)
            selectedTab = 1
        }
    }

    // Force tab 1 if a room was just selected from the list
    val roomSelected = currentRoom != null
    LaunchedEffect(roomSelected) {
        if (roomSelected && selectedTab == 0) {
            selectedTab = 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BgSurface,
            contentColor = VioletPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = VioletPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Salons", color = if (selectedTab == 0) TextPrimary else TextSecondary) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(currentRoom?.name ?: "Chat", color = if (selectedTab == 1) TextPrimary else TextSecondary) },
                enabled = currentRoom != null
            )
        }

        if (selectedTab == 0) {
            RoomList(rooms) { room ->
                viewModel.selectRoom(room)
                selectedTab = 1
            }
        } else {
            val currentUserId by viewModel.currentUserId.collectAsState()
            ChatView(
                messages = messages,
                reactions = reactions,
                currentUserId = currentUserId,
                onSendMessage = { viewModel.sendMessage(it) },
                onReact = { msgId, emoji -> viewModel.reactToMessage(msgId, emoji) }
            )
        }
    }
}

@Composable
fun RoomList(rooms: List<ChatRoom>, onRoomClick: (ChatRoom) -> Unit) {
    if (rooms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucun salon disponible", color = TextSecondary)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rooms) { room ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRoomClick(room) },
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = room.description ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(VioletPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("0", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatView(
    messages: List<ChatWSMessage>,
    reactions: List<com.example.sensai.data.network.dto.chat.ChatWSReaction>,
    currentUserId: Long,
    onSendMessage: (String) -> Unit,
    onReact: (Long, String) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    var reactionDialogMessageId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                val messageReactions = reactions.filter { it.messageId == message.id }
                MessageBubble(
                    message = message,
                    reactions = messageReactions,
                    isCurrentUser = message.userId == currentUserId,
                    onLongPress = { reactionDialogMessageId = message.id }
                )
            }
        }

        InputBar(
            text = textState,
            onTextChange = { textState = it },
            onSend = {
                onSendMessage(textState)
                textState = ""
            }
        )
    }

    if (reactionDialogMessageId != null) {
        EmojiReactionRow(
            onEmojiSelected = { emoji ->
                onReact(reactionDialogMessageId!!, emoji)
                reactionDialogMessageId = null
            },
            onDismiss = { reactionDialogMessageId = null }
        )
    }
}

@Composable
fun MessageBubble(
    message: ChatWSMessage,
    reactions: List<com.example.sensai.data.network.dto.chat.ChatWSReaction>,
    isCurrentUser: Boolean,
    onLongPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            },
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) VioletPrimary else BgCard,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 12.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isCurrentUser) {
                    Text(
                        text = message.username,
                        style = MaterialTheme.typography.labelSmall,
                        color = VioletLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCurrentUser) Color.White else TextPrimary
                )
            }
        }
        
        if (reactions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val grouped = reactions.groupBy { it.emoji }
                grouped.forEach { (emoji, list) ->
                    Surface(
                        color = BgElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "$emoji ${list.size}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = BgSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...", color = TextMuted) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Send,
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
                    autoCorrect = false,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSend = { if (text.isNotBlank()) onSend() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = VioletPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = VioletPrimary,
                    disabledContainerColor = BgElevated
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Envoyer", tint = Color.White)
            }
        }
    }
}

@Composable
fun EmojiReactionRow(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val emojis = listOf("❤️", "🔥", "😂", "😮", "😢", "👍")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = BgCard,
        title = { Text("Réagir", color = TextPrimary) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        modifier = Modifier
                            .clickable { onEmojiSelected(emoji) }
                            .padding(8.dp),
                        fontSize = 24.sp
                    )
                }
            }
        }
    )
}
