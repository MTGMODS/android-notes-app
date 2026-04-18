package com.mtg.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    savedName: String,
    onEnterNameClick: () -> Unit,
    onStartClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "📝", fontSize = 100.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Notes App",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(onClick = onEnterNameClick) {
            Text("Ввести ім'я")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onStartClick(savedName) },
            enabled = savedName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (savedName.isNotBlank()) "Привіт, $savedName! Розпочати" else "Розпочати")
        }
    }
}

@Composable
fun NameInputScreen(onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ваше ім'я") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(name) },
            enabled = name.isNotBlank()
        ) {
            Text("Зберегти")
        }
    }
}