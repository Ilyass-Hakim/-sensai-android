package com.example.sensai.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.data.network.dto.chat.ChatRoom
import com.example.sensai.data.network.dto.chat.ChatWSMessage
import com.example.sensai.ui.theme.*

// ── Local accent palette for room cards ──────────────────────────────────────
private val AccentGeneral  = Color(0xFF7C3AED)   // violet
private val AccentSpoiler  = Color(0xFFEF4444)   // red-orange
private val AccentTheory   = Color(0xFF3B82F6)   // blue
private val AccentReview   = Color(0xFF10B981)   // emerald
private val AccentFallback = Color(0xFF8B5CF6)   // soft violet

private fun roomAccent(room: ChatRoom): Color = when {
    room.name.contains("spoil", ignoreCase = true) ||
    room.type.contains("spoil", ignoreCase = true) -> AccentSpoiler
    room.name.contains("theor", ignoreCase = true) ||
    room.type.contains("theor", ignoreCase = true) -> AccentTheory
    room.name.contains("review", ignoreCase = true) ||
    room.name.contains("avis", ignoreCase = true) -> AccentReview
    room.name.contains("general", ignoreCase = true) ||
    room.animeId == null -> AccentGeneral
    else -> AccentFallback
}

private fun roomIcon(room: ChatRoom): ImageVector = when {
    room.name.contains("spoil", ignoreCase = true) ||
    room.type.contains("spoil", ignoreCase = true) -> Icons.Default.LocalFireDepartment
    room.name.contains("theor", ignoreCase = true) ||
    room.type.contains("theor", ignoreCase = true) -> Icons.Default.WbIncandescent
    room.name.contains("general", ignoreCase = true) ||
    room.animeId == null -> Icons.Default.ChatBubble
    room.name.contains("review", ignoreCase = true) -> Icons.Default.EmojiEvents
    else -> Icons.Default.Explore
}

private fun roomActivityTag(room: ChatRoom): String = when {
    room.name.contains("spoil", ignoreCase = true) -> "Hot"
    room.name.contains("general", ignoreCase = true) -> "Live"
    room.animeId != null -> "Active"
    else -> "Open"
}

// ── Screen ────────────────────────────────────────────────────────────────────
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
        // ── Animated pill tab row ─────────────────────────────────────────────
        EnhancedTabRow(
            selectedTab = selectedTab,
            chatLabel = currentRoom?.name ?: "Chat",
            chatEnabled = currentRoom != null,
            onSelectTab = { selectedTab = it }
        )

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

// ── Animated pill tab indicator ───────────────────────────────────────────────
@Composable
private fun EnhancedTabRow(
    selectedTab: Int,
    chatLabel: String,
    chatEnabled: Boolean,
    onSelectTab: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSurface)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(BgCard)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            listOf("Salons", chatLabel).forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                val enabled = index == 0 || chatEnabled

                val bgAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    label = "tab_bg_$index"
                )
                val textAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(200),
                    label = "tab_text_$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .then(
                            if (isSelected) Modifier.background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF4F46E5), VioletPrimary)
                                )
                            ) else Modifier
                        )
                        .clickable(enabled = enabled) { onSelectTab(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.6.sp,
                        color = if (isSelected) Color.White else TextSecondary.copy(alpha = textAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ── Room list with atmospheric background ─────────────────────────────────────
@Composable
fun RoomList(rooms: List<ChatRoom>, onRoomClick: (ChatRoom) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Atmospheric background dots pattern
        AtmosphericBackground()

        if (rooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = VioletPrimary.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aucun salon disponible",
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(rooms) { index, room ->
                    // Staggered entrance animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 80L)
                        visible = true
                    }
                    val cardAlpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(350, easing = FastOutSlowInEasing),
                        label = "card_alpha_$index"
                    )
                    val cardOffset by animateFloatAsState(
                        targetValue = if (visible) 0f else 32f,
                        animationSpec = tween(350, easing = FastOutSlowInEasing),
                        label = "card_offset_$index"
                    )

                    Box(modifier = Modifier.offset(y = cardOffset.dp)) {
                        RoomCard(
                            room = room,
                            alpha = cardAlpha,
                            onClick = { onRoomClick(room) }
                        )
                    }
                }

                // Breathing room below cards
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// ── Individual room card ───────────────────────────────────────────────────────
@Composable
private fun RoomCard(room: ChatRoom, alpha: Float, onClick: () -> Unit) {
    val accent = roomAccent(room)
    val icon   = roomIcon(room)
    val tag    = roomActivityTag(room)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = accent.copy(alpha = 0.18f),
                spotColor = accent.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BgCard,
                        BgElevated.copy(alpha = 0.85f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            // Glowing left accent border
            .drawBehind {
                drawRect(
                    color = accent,
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                )
            }
            .clickable { onClick() }
            .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Themed room icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.15f))
                    .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!room.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = room.description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                // Activity tag pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "● $tag",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Pulsing notification badge
            PulsingBadge(accent = accent)
        }
    }
}

// ── Pulsing notification badge ────────────────────────────────────────────────
@Composable
private fun PulsingBadge(accent: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_glow"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size((28 * scale).dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = glowAlpha))
        )
        // Badge chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        listOf(accent, accent.copy(alpha = 0.75f))
                    )
                )
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("0", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Atmospheric dot-grid background ──────────────────────────────────────────
@Composable
private fun AtmosphericBackground() {
    // Dot-grid drawn directly on the full-size canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val dotColor = Color(0xFF7C3AED).copy(alpha = 0.06f)
                val spacing = 32.dp.toPx()
                val dotRadius = 1.5.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                        y += spacing
                    }
                    x += spacing
                }
            }
    )

    // Ambient glow blobs — use a fillMaxSize Box so BoxScope.align() is available
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VioletPrimary.copy(alpha = 0.07f), Color.Transparent)
                    ),
                    CircleShape
                )
                .blur(40.dp)
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentTheory.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
                .blur(40.dp)
        )
    }
}

// ── Chat message view (unchanged logic, minor visual polish) ──────────────────
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


