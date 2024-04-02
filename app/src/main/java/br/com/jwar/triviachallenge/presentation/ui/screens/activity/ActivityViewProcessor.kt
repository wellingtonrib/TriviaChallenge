package br.com.jwar.triviachallenge.presentation.ui.screens.activity

import br.com.jwar.triviachallenge.R
import br.com.jwar.triviachallenge.domain.repositories.ActivityRepository
import br.com.jwar.triviachallenge.presentation.utils.UIMessage
import br.com.jwar.triviachallenge.presentation.utils.UIMessageStyle
import br.com.jwar.triviachallenge.presentation.utils.UIText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface ActivityViewProcessor {

    val activityRepository: ActivityRepository
    val scope: CoroutineScope

    fun getStateByCheckingAnswer(state: ActivityViewState.Loaded): ActivityViewState.Loaded {
        val isCorrectAnswer = state.selectedAnswer == state.currentQuestion.correctAnswer
        val attemptsLeft = if (isCorrectAnswer) state.attemptsLeft else state.attemptsLeft - 1
        val points = if (isCorrectAnswer) state.points + 1 else state.points
        val messageRes = if (isCorrectAnswer) R.string.message_correct_answer else R.string.message_wrong_answer
        val messageStyle = if (isCorrectAnswer) UIMessageStyle.SUCCESS else UIMessageStyle.DANGER
        return state.copy(
            isResultShown = true,
            attemptsLeft = attemptsLeft,
            points = points,
            userMessages = state.userMessages + UIMessage(
                text = UIText.StringResource(messageRes),
                style = messageStyle
            )
        )
    }

    fun getStateByCheckingNextQuestion(state: ActivityViewState.Loaded): ActivityViewState.Loaded {
        val hasAttemptsRemaining = state.attemptsLeft > 0
        val currentQuestionIndex = state.questions.indexOf(state.currentQuestion)
        val hasNextQuestion = currentQuestionIndex < state.questions.size - 1
        return if (hasAttemptsRemaining && hasNextQuestion) {
            val nextQuestionIndex = currentQuestionIndex + 1
            val nextQuestion = state.questions[nextQuestionIndex]
            val progress = "${nextQuestionIndex + 1}/${state.questions.size}"
            state.copy(
                currentQuestion = nextQuestion,
                selectedAnswer = null,
                isResultShown = false,
                progress = progress,
            )
        } else {
            if (hasAttemptsRemaining) {
                completeActivity(state.activityId)
            }
            state.copy(
                isFinished = true,
                isSucceeded = hasAttemptsRemaining,
            )
        }
    }

    private fun completeActivity(activityId: String) = scope.launch {
        activityRepository.completeActivity(activityId)
    }
}