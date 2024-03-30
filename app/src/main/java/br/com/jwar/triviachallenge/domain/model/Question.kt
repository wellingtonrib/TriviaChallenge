package br.com.jwar.triviachallenge.domain.model

data class Question(
    val id: String,
    val unit: String,
    val correctAnswer: String,
    val difficulty: String,
    val answers: List<String>,
    val question: String,
    val type: String
)