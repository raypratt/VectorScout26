package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.Event
import com.example.vectorscout26.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreMatchSection(
    events: List<Event>,
    event: String,
    matchNumber: String,
    robotDesignation: String,
    scoutName: String,
    teamNumber: String,
    startPosition: String,
    loaded: Boolean,
    noShow: Boolean,
    isBlueRight: Boolean,
    teamNumberAutoFilled: Boolean = false,
    onEventChange: (eventCode: String, eventName: String) -> Unit,
    onMatchNumberChange: (String) -> Unit,
    onRobotDesignationChange: (String) -> Unit,
    onScoutNameChange: (String) -> Unit,
    onTeamNumberChange: (String) -> Unit,
    onStartPositionChange: (String) -> Unit,
    onLoadedToggle: () -> Unit,
    onNoShowToggle: () -> Unit,
    onToggleOrientation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var designationExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Pre-Match",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            EventSelector(
                events = events,
                selectedEventCode = event,
                onEventSelected = { eventCode ->
                    val eventName = events.find { it.eventCode == eventCode }?.eventName ?: ""
                    onEventChange(eventCode, eventName)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = matchNumber,
                    onValueChange = onMatchNumberChange,
                    label = { Text("Match #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                ExposedDropdownMenuBox(
                    expanded = designationExpanded,
                    onExpandedChange = { designationExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = robotDesignation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Position") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = designationExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = designationExpanded,
                        onDismissRequest = { designationExpanded = false }
                    ) {
                        Constants.ROBOT_DESIGNATIONS.forEach { designation ->
                            DropdownMenuItem(
                                text = { Text(designation) },
                                onClick = {
                                    onRobotDesignationChange(designation)
                                    designationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = scoutName,
                    onValueChange = onScoutNameChange,
                    label = { Text("Scout Name") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = teamNumber,
                    onValueChange = onTeamNumberChange,
                    label = { Text(if (teamNumberAutoFilled) "Team # (auto)" else "Team #") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = if (teamNumberAutoFilled) {
                        OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        OutlinedTextFieldDefaults.colors()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Start Position",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            StartLocationSelector(
                selectedLocation = startPosition,
                onLocationSelected = onStartPositionChange,
                robotDesignation = robotDesignation,
                isBlueRight = isBlueRight,
                onToggleOrientation = onToggleOrientation,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = loaded,
                    onCheckedChange = { onLoadedToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Loaded with fuel",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = noShow,
                    onCheckedChange = { onNoShowToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "No Show",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
