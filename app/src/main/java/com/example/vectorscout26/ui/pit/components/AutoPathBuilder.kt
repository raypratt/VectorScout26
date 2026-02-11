package com.example.vectorscout26.ui.pit.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.*
import com.example.vectorscout26.utils.Constants

@Composable
fun AutoPathBuilder(
    steps: List<AutoPathStep>,
    currentCategory: PathCategory,
    locationOptions: List<String>,
    onAddStep: (AutoPathStep) -> Unit,
    onDeleteStepAndAfter: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Path display - horizontal scrollable row of step buttons
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto Path",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (steps.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear path",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (steps.isEmpty()) {
                    Text(
                        text = "Tap a start position to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        steps.forEachIndexed { index, step ->
                            val color = when (step.type) {
                                StepType.START -> MaterialTheme.colorScheme.primary
                                StepType.ACTION -> MaterialTheme.colorScheme.secondary
                                StepType.LOCATION -> MaterialTheme.colorScheme.tertiary
                            }

                            ElevatedFilterChip(
                                selected = false,
                                onClick = { onDeleteStepAndAfter(index) },
                                label = { Text(step.value) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    containerColor = color.copy(alpha = 0.2f),
                                    labelColor = color
                                )
                            )

                            if (index < steps.size - 1) {
                                Text(
                                    "→",
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category selector
        PathCategorySelector(
            currentCategory = currentCategory,
            locationOptions = locationOptions,
            onSelect = onAddStep
        )
    }
}

@Composable
fun PathCategorySelector(
    currentCategory: PathCategory,
    locationOptions: List<String>,
    onSelect: (AutoPathStep) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val title = when (currentCategory) {
            PathCategory.START -> "Select Start Position"
            PathCategory.ACTION -> "Select Action"
            PathCategory.LOCATION -> "Select Location"
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (currentCategory) {
            PathCategory.START -> {
                FlowButtonRow(
                    options = Constants.PIT_START_POSITIONS,
                    onSelect = { value ->
                        onSelect(AutoPathStep(StepType.START, value))
                    },
                    buttonColor = MaterialTheme.colorScheme.primary
                )
            }
            PathCategory.ACTION -> {
                FlowButtonRow(
                    options = Constants.PATH_ACTIONS,
                    onSelect = { value ->
                        onSelect(AutoPathStep(StepType.ACTION, value))
                    },
                    buttonColor = MaterialTheme.colorScheme.secondary
                )
            }
            PathCategory.LOCATION -> {
                FlowButtonRow(
                    options = locationOptions,
                    onSelect = { value ->
                        onSelect(AutoPathStep(StepType.LOCATION, value))
                    },
                    buttonColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun FlowButtonRow(
    options: List<String>,
    onSelect: (String) -> Unit,
    buttonColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    // Arrange buttons in rows of 3-4
    val chunkedOptions = options.chunked(4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chunkedOptions.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { option ->
                    Button(
                        onClick = { onSelect(option) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor
                        )
                    ) {
                        Text(option)
                    }
                }
                // Fill empty slots with spacers
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
