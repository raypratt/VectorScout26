package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSelector(
    events: List<Event>,
    selectedEventCode: String,
    onEventSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Find the selected event to display its name
    val selectedEvent = events.find { it.eventCode == selectedEventCode }
    val displayText = selectedEvent?.let { "${it.eventName} (${it.date})" } ?: "Select Event"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            events.forEach { event ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = event.eventName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = event.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onEventSelected(event.eventCode)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
