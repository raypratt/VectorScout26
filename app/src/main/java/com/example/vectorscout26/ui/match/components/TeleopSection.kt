package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.ActionType
import com.example.vectorscout26.ui.theme.ActionOrange
import com.example.vectorscout26.ui.theme.ActionPurple

@Composable
fun TeleopSection(
    loadCount: Int,
    shootCount: Int,
    ferryCount: Int,
    defenseCount: Int,
    incapacitatedCount: Int,
    tippedCount: Int,
    onActionClick: (ActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Teleop",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
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
                label = "Defense",
                count = defenseCount,
                onClick = { onActionClick(ActionType.Defense) },
                containerColor = ButtonDefaults.buttonColors(containerColor = ActionOrange)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    label = "Foul",
                    count = 0,
                    onClick = { onActionClick(ActionType.Foul) },
                    modifier = Modifier.weight(1f),
                    containerColor = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                )

                ActionButton(
                    label = "Damaged",
                    count = 0,
                    onClick = { onActionClick(ActionType.Damaged) },
                    modifier = Modifier.weight(1f),
                    containerColor = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    label = "Incapacitated",
                    count = incapacitatedCount,
                    onClick = { onActionClick(ActionType.Incapacitated) },
                    modifier = Modifier.weight(1f),
                    containerColor = ButtonDefaults.buttonColors(containerColor = ActionPurple)
                )

                ActionButton(
                    label = "Tipped",
                    count = tippedCount,
                    onClick = { onActionClick(ActionType.Tipped) },
                    modifier = Modifier.weight(1f),
                    containerColor = ButtonDefaults.buttonColors(containerColor = ActionOrange)
                )
            }
        }
    }
}
