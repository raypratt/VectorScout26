package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.ActionType
import com.example.vectorscout26.ui.theme.ActionPurple

@Composable
fun EndGameSection(
    climbCount: Int,
    onActionClick: (ActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "End Game",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionButton(
                label = "Climb",
                count = climbCount,
                onClick = { onActionClick(ActionType.EndGameClimb) },
                containerColor = ButtonDefaults.buttonColors(containerColor = ActionPurple)
            )
        }
    }
}
