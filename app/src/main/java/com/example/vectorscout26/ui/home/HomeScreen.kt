package com.example.vectorscout26.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onMatchScoutingClick: () -> Unit,
    onPitScoutingClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "VectorScout26",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FRC 2026 Rebuild",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onMatchScoutingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Match Scouting",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 33.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onPitScoutingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Pit Scouting",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 33.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "(Pit Scouting - Coming Soon)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
