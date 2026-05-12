package com.example.sensai.ui.screens.quizhistory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.data.network.dto.quiz.QuizSessionDto
import com.example.sensai.ui.theme.BgCard
import com.example.sensai.ui.theme.BgDeep
import com.example.sensai.ui.theme.BgElevated
import com.example.sensai.ui.theme.TextMuted
import com.example.sensai.ui.theme.TextPrimary
import com.example.sensai.ui.theme.TextSecondary
import com.example.sensai.ui.theme.VioletPrimary
import com.example.sensai.ui.theme.VioletLight
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Quiz History",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadHistory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep)
            )
        },
        containerColor = BgDeep
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VioletPrimary)
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⚠️ ${uiState.error}",
                        color = Color(0xFFF44336),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                    ) {
                        Text("Retry")
                    }
                }
            }
        } else if (uiState.sessions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = TextMuted
                    )
                    Text("No quizzes completed yet", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Complete a daily quiz to see your stats here!", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Stats Card
                item {
                    StatsOverviewCard(uiState = uiState)
                }

                item {
                    Text(
                        text = "Past Sessions",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Session list
                itemsIndexed(uiState.sessions, key = { _, session -> session.id }) { _, session ->
                    QuizSessionCard(session = session)
                }
            }
        }
    }
}

@Composable
private fun StatsOverviewCard(uiState: QuizHistoryUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(VioletPrimary.copy(alpha = 0.8f), VioletLight.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                    Text("Your Quiz Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBubble(value = "${uiState.totalGames}", label = "Games")
                    StatBubble(value = "${uiState.bestScore}", label = "Best Score")
                    StatBubble(value = "${uiState.averageAccuracy.roundToInt()}%", label = "Accuracy")
                    StatBubble(value = "${uiState.totalXp}", label = "Total XP")
                }
            }
        }
    }
}

@Composable
private fun StatBubble(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}

@Composable
private fun QuizSessionCard(session: QuizSessionDto) {
    val accuracy = if (session.totalQuestions > 0)
        (session.score.toFloat() / session.totalQuestions * 100).roundToInt()
    else 0

    val accuracyColor = when {
        accuracy >= 80 -> Color(0xFF4CAF50)
        accuracy >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    // Animated accuracy bar
    var startAnimation by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnimation) accuracy / 100f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "accuracy"
    )
    LaunchedEffect(Unit) { startAnimation = true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatDate(session.completedAt),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${session.score} / ${session.totalQuestions} correct",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
                // XP badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+${session.score * 2} XP",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Accuracy bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Accuracy", color = TextMuted, fontSize = 12.sp)
                Text("$accuracy%", color = accuracyColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accuracyColor,
                trackColor = BgElevated
            )
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return "Unknown date"
    return try {
        val dt = LocalDateTime.parse(dateString.take(19)) // truncate to "yyyy-MM-ddTHH:mm:ss"
        dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy • HH:mm"))
    } catch (e: DateTimeParseException) {
        dateString.take(10) // fallback: just the date portion
    }
}
