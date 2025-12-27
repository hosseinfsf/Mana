package com.example.mana.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mana.ui.theme.ManaTheme

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onboardingViewModel: OnboardingViewModel = viewModel(),
    onOnboardingComplete: () -> Unit
) {
    val uiState by onboardingViewModel.uiState.collectAsState()

    if (uiState.isCompleted) {
        onOnboardingComplete()
    }

    AnimatedContent(
        targetState = uiState.currentQuestionIndex,
        transitionSpec = {
            slideInHorizontally { height -> height } + fadeIn() with
                slideOutHorizontally { height -> -height } + fadeOut()
        }
    ) { targetIndex ->
        val question = onboardingViewModel.questions[targetIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            when (question.type) {
                QuestionType.TEXT_INPUT -> TextInputQuestion(onboardingViewModel)
                QuestionType.SINGLE_CHOICE -> SingleChoiceQuestion(question.options, onboardingViewModel)
            }
        }
    }
}

@Composable
fun TextInputQuestion(viewModel: OnboardingViewModel) {
    var answer by remember { mutableStateOf(TextFieldValue("")) }

    OutlinedTextField(
        value = answer,
        onValueChange = { answer = it },
        label = { Text("پاسخ شما...") },
        singleLine = true
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = {
            viewModel.onAnswer(answer.text)
            viewModel.onNext()
        },
        enabled = answer.text.isNotBlank()
    ) {
        Text("بعدی")
    }
}

@Composable
fun SingleChoiceQuestion(options: List<String>, viewModel: OnboardingViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) {
            option ->
            Button(onClick = {
                viewModel.onAnswer(option)
                viewModel.onNext()
            }) {
                Text(option, textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    ManaTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}
