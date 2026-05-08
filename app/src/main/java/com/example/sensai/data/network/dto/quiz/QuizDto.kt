package com.example.sensai.data.network.dto.quiz

import com.google.gson.annotations.SerializedName

data class QuizQuestionDto(
    val id: Long,
    val animeId: Int,
    val question: String,
    val correctAnswer: String,
    val wrongAnswers: List<String>,
    val difficulty: String
)

data class QuizSubmitDto(
    val score: Int,
    val totalQuestions: Int
)

data class QuizSessionDto(
    val id: Long,
    val score: Int,
    val totalQuestions: Int,
    val completedAt: String?
)
