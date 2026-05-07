package com.example.sensai.ui.sensei

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.util.AudioPlayer
import com.example.sensai.ui.theme.*

@Composable
fun SenseiChatScreen(
    viewModel: SenseiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.audioEvents.collect { base64 ->
            AudioPlayer.playBase64(context, base64)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Top 40% - Sensei Presentation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BgCard)
                    .border(2.dp, VioletPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("S", fontSize = 48.sp, color = VioletPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sensei AI",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ton guide anime personnel",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        // Curved Separation and Bottom 60%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f) // Slightly more to cover the curve overlap
                .align(Alignment.BottomCenter)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, 60.dp.toPx())
                    quadraticTo(
                        size.width / 2, 0f,
                        size.width, 60.dp.toPx()
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, color = BgSurface)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp) // Start below the curve top
            ) {
                // Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
                ) {
                    if (uiState.isTyping) {
                        item {
                            TypingIndicatorBubble()
                        }
                    }
                    items(uiState.messages) { message ->
                        MessageBubble(message)
                    }
                }

                // Input Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSurface)
                        .padding(16.dp)
                ) {
                    CommandChips(onChipClick = { viewModel.onInputChange(it) })
                    Spacer(modifier = Modifier.height(8.dp))
                    InputBar(
                        text = uiState.inputText,
                        isVoiceEnabled = uiState.isVoiceEnabled,
                        onTextChange = { viewModel.onInputChange(it) },
                        onSend = { viewModel.sendMessage() },
                        onVoiceToggle = { viewModel.toggleVoice() }
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isFromUser) VioletPrimary else BgCard
    val textColor = if (message.isFromUser) Color.White else TextPrimary
    val shape = if (message.isFromUser) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bgColor,
            shape = shape,
            border = if (!message.isFromUser) BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f)) else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun TypingIndicatorBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = BgCard,
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
            border = BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f)),
            modifier = Modifier.width(80.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    TypingDot(index)
                }
            }
        }
    }
}

@Composable
fun TypingDot(index: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(VioletPrimary.copy(alpha = alpha))
    )
}

@Composable
fun CommandChips(onChipClick: (String) -> Unit) {
    val commands = listOf("/recommande", "/quiz", "/infos", "/humeur")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(commands) { command ->
            Surface(
                modifier = Modifier.clickable { onChipClick(command) },
                color = BgCard,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, VioletPrimary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = command,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputBar(
    text: String,
    isVoiceEnabled: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onVoiceToggle,
            modifier = Modifier
                .size(44.dp)
                .background(if (isVoiceEnabled) VioletPrimary.copy(alpha = 0.1f) else Color.Transparent, CircleShape)
        ) {
            Icon(
                imageVector = if (isVoiceEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = "Toggle Voice",
                tint = if (isVoiceEnabled) VioletPrimary else TextSecondary
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = { Text("Posez votre question...", color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
                focusedBorderColor = VioletPrimary,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3
        )

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(44.dp)
                .background(VioletPrimary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Envoyer",
                tint = Color.White
            )
        }
    }
}
