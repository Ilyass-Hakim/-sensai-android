package com.example.sensai.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sensai.ui.theme.VioletPrimary

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading && !uiState.isFinished) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VioletPrimary)
        }
    } else if (uiState.isFinished) {
        ScoreScreen(uiState = uiState, onNavigateBack = onNavigateBack)
    } else if (uiState.questions.isNotEmpty()) {
        val currentQuestion = uiState.questions[uiState.currentQuestionIndex]
        // Combine answers and shuffle them only once per question
        val allAnswers = remember(currentQuestion) {
            (currentQuestion.wrongAnswers + currentQuestion.correctAnswer).shuffled()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${uiState.currentQuestionIndex + 1}/${uiState.questions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Score: ${uiState.score}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = VioletPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = VioletPrimary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Timer
                TimerArc(timeLeft = uiState.timeLeft)

                Spacer(modifier = Modifier.height(32.dp))

                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Answers
                allAnswers.forEach { answer ->
                    AnswerButton(
                        text = answer,
                        isSelected = uiState.selectedAnswer == answer,
                        isCorrect = answer == currentQuestion.correctAnswer,
                        showFeedback = uiState.selectedAnswer != null,
                        onClick = { viewModel.submitAnswer(answer) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // XP Animation Overlay
            AnimatedVisibility(
                visible = uiState.showXpAnimation,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "+20 XP",
                    color = Color(0xFFFFD700), // Gold
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (uiState.error != null) {
                Text(text = uiState.error!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                Text("No quiz available for today.")
            }
        }
    }
}

@Composable
fun TimerArc(timeLeft: Int) {
    val progress by animateFloatAsState(
        targetValue = timeLeft / 15f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timer"
    )
    val color = if (timeLeft <= 5) Color.Red else VioletPrimary

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 270f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = timeLeft.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showFeedback && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f) // Green/15%
        showFeedback && isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.15f) // Red/15%
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when {
        showFeedback && isCorrect -> Color(0xFF4CAF50)
        showFeedback && isSelected && !isCorrect -> Color(0xFFF44336)
        else -> VioletPrimary.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !showFeedback, onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (showFeedback && isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ScoreScreen(uiState: QuizUiState, onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Terminé !",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "${uiState.score}",
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold,
            color = VioletPrimary
        )
        Text(
            text = "Points",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // XP and Rank info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "XP Gagnés", color = Color.Gray)
                Text(
                    text = "+${uiState.score * 2} XP",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFFD700), // Gold
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Retour à l'accueil", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
