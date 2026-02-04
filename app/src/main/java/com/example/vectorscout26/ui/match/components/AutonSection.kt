package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.ActionType
import com.example.vectorscout26.data.model.MatchPhase
import com.example.vectorscout26.ui.theme.ActionOrange
import com.example.vectorscout26.ui.theme.ActionPurple

@Composable
fun AutonSection(
    loadCount: Int,
    shootCount: Int,
    ferryCount: Int,
    climbCount: Int,
    onActionClick: (ActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Autonomous",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            ActionButton(
                label = "Load",
                count = loadCount,
                onClick = { onActionClick(ActionType.Load) },
                containerColor = ButtonDefaults.buttonColors(containerColor = ActionPurple)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                label = "Shoot",
                count = shootCount,
                onClick = { onActionClick(ActionType.Shoot) },
                containerColor = ButtonDefaults.buttonColors(containerColor = ActionOrange)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                label = "Ferry",
                count = ferryCount,
                onClick = { onActionClick(ActionType.Ferry) },
                containerColor = ButtonDefaults.buttonColors(containerColor = ActionPurple)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                label = "Climb",
                count = climbCount,
                onClick = { onActionClick(ActionType.AutonClimb) },
                containerColor = ButtonDefaults.buttonColors(containerColor = ActionOrange)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                label = "Foul",
                count = 0,
                onClick = { onActionClick(ActionType.Foul) },
                containerColor = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}
