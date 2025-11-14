package com.example.summaryquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.summaryquiz.data.QuizAttempt
import com.example.summaryquiz.model.QuizQuestion
import com.example.summaryquiz.viewmodel.QuizViewModel
import com.example.summaryquiz.viewmodel.QuizViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: QuizViewModel by viewModels {
        QuizViewModelFactory((application as QuizApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SummaryQuizApp(viewModel = viewModel)
        }
    }
}

@Composable
fun SummaryQuizApp(viewModel: QuizViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Summary Quiz Studio") })
        }
    ) { padding ->
        QuizContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            uiState = uiState,
            history = history,
            onTextChange = viewModel::updateInputText,
            onGenerate = viewModel::generateQuiz,
            onAnswer = viewModel::selectAnswer,
            onResetQuiz = viewModel::resetCurrentQuiz
        )
    }
}

@Composable
private fun QuizContent(
    modifier: Modifier,
    uiState: com.example.summaryquiz.ui.QuizUiState,
    history: List<QuizAttempt>,
    onTextChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onAnswer: (Int, Int) -> Unit,
    onResetQuiz: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InputSection(
            text = uiState.inputText,
            wordCount = uiState.wordCount,
            isGenerating = uiState.isGenerating,
            onTextChange = onTextChange,
            onGenerate = onGenerate
        )

        AnimatedVisibility(visible = uiState.summary.isNotBlank()) {
            SummarySection(summary = uiState.summary, onReset = onResetQuiz)
        }

        AnimatedVisibility(visible = uiState.questions.isNotEmpty()) {
            QuizSection(
                questions = uiState.questions,
                answers = uiState.answers,
                onAnswer = onAnswer
            )
        }

        HistorySection(history = history)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputSection(
    text: String,
    wordCount: Int,
    isGenerating: Boolean,
    onTextChange: (String) -> Unit,
    onGenerate: () -> Unit
) {
    OutlinedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Paste at least 200 words of study material.",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                placeholder = { Text("Enter or paste your notes here...") },
                supportingText = {
                    Text("Word count: $wordCount / 200+")
                },
                minLines = 8,
                maxLines = 10
            )

            Button(
                onClick = onGenerate,
                enabled = !isGenerating && wordCount >= 200,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Summarizing and generating quiz...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Create Summary & Quiz")
                }
            }
        }
    }
}

@Composable
private fun SummarySection(summary: String, onReset: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("AI Summary", style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = onReset,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Clear summary & quiz")
            }
        }
    }
}

@Composable
private fun QuizSection(
    questions: List<QuizQuestion>,
    answers: Map<Int, Int>,
    onAnswer: (Int, Int) -> Unit
) {
    val score = questions.indices.count { index ->
        answers[index] == questions[index].answerIndex
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quiz (${questions.size} questions)",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Your score: $score / ${questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        questions.forEachIndexed { index, question ->
            QuizQuestionCard(
                index = index,
                question = question,
                selectedOption = answers[index],
                onAnswerSelected = onAnswer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizQuestionCard(
    index: Int,
    question: QuizQuestion,
    selectedOption: Int?,
    onAnswerSelected: (Int, Int) -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Q${index + 1}: ${question.prompt}",
                style = MaterialTheme.typography.titleSmall
            )
            question.options.forEachIndexed { optionIndex, option ->
                val isCorrect = optionIndex == question.answerIndex
                val isSelected = optionIndex == selectedOption

                val background = when {
                    isSelected && isCorrect -> MaterialTheme.colorScheme.primaryContainer
                    isSelected -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor = when {
                    isSelected && isCorrect -> MaterialTheme.colorScheme.onPrimaryContainer
                    isSelected -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(background),
                    onClick = { onAnswerSelected(index, optionIndex) },
                    enabled = selectedOption == null
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(12.dp),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(history: List<QuizAttempt>) {
    var isDialogOpen by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Recent Sessions", style = MaterialTheme.typography.titleMedium)

            if (history.isEmpty()) {
                Text(
                    text = "No saved quizzes yet. Generate one to start building your study record.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                history.take(3).forEach { attempt ->
                    HistoryRow(attempt = attempt)
                }

                TextButton(onClick = { isDialogOpen = true }) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("View all history")
                }
            }
        }
    }

    if (isDialogOpen) {
        HistoryDialog(history = history, onDismiss = { isDialogOpen = false })
    }
}

@Composable
private fun HistoryRow(attempt: QuizAttempt) {
    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = formatter.format(Date(attempt.createdAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = attempt.summary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HistoryDialog(history: List<QuizAttempt>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = { Text("Stored quiz sessions (${history.size})") },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(history) { index, attempt ->
                    HistoryCard(attempt = attempt, index = index + 1)
                }
            }
        }
    )
}

@Composable
private fun HistoryCard(attempt: QuizAttempt, index: Int) {
    val formatter = remember {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    }
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Session $index", fontWeight = FontWeight.SemiBold)
            Text(formatter.format(Date(attempt.createdAt)), style = MaterialTheme.typography.bodySmall)
            Text(
                text = attempt.summary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

