package com.example.vectorscout26.ui.match.action.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.vectorscout26.data.model.*
import com.example.vectorscout26.ui.match.components.FerryLocationSelector
import com.example.vectorscout26.ui.match.components.FieldMapSelector
import com.example.vectorscout26.ui.match.components.LoadLocationSelector
import com.example.vectorscout26.ui.match.components.ShootLocationSelector
import com.example.vectorscout26.utils.Constants

// Load Form
@Composable
fun LoadForm(
    robotDesignation: String,
    isBlueRight: Boolean,
    onDataChange: (LoadData) -> Unit
) {
    var selectedZone by remember { mutableStateOf("") }

    LaunchedEffect(selectedZone) {
        if (selectedZone.isNotEmpty()) {
            onDataChange(LoadData(loadLocation = selectedZone))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Load Location",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        LoadLocationSelector(
            selectedLocation = selectedZone,
            onLocationSelected = { selectedZone = it },
            robotDesignation = robotDesignation,
            isBlueRight = isBlueRight
        )
    }
}

// Shoot Form
@Composable
fun ShootForm(
    robotDesignation: String,
    isBlueRight: Boolean,
    onDataChange: (ShootData) -> Unit
) {
    var location by remember { mutableStateOf("") }

    LaunchedEffect(location) {
        if (location.isNotEmpty()) {
            onDataChange(ShootData(location = location))
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Location Selection
        Text("Location", style = MaterialTheme.typography.titleMedium)
        ShootLocationSelector(
            selectedLocation = location,
            onLocationSelected = { location = it },
            robotDesignation = robotDesignation,
            isBlueRight = isBlueRight
        )
    }
}

// Ferry Form
@Composable
fun FerryForm(
    robotDesignation: String,
    isBlueRight: Boolean,
    onDataChange: (FerryData) -> Unit
) {
    var ferryType by remember { mutableStateOf("") }
    var ferryDelivery by remember { mutableStateOf("") }

    LaunchedEffect(ferryType, ferryDelivery) {
        if (ferryType.isNotEmpty() && ferryDelivery.isNotEmpty()) {
            onDataChange(FerryData(ferryType = ferryType, ferryDelivery = ferryDelivery))
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ferry Type", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Constants.FERRY_TYPES.forEach { type ->
                FilterChip(
                    selected = ferryType == type,
                    onClick = { ferryType = type },
                    label = { Text(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Text("Ferry Delivery", style = MaterialTheme.typography.titleMedium)
        FerryLocationSelector(
            selectedLocation = ferryDelivery,
            onLocationSelected = { ferryDelivery = it },
            robotDesignation = robotDesignation,
            isBlueRight = isBlueRight
        )
    }
}

// Climb Form
@Composable
fun ClimbForm(
    phase: MatchPhase,
    onDataChange: (ClimbData) -> Unit
) {
    var result by remember { mutableStateOf("") }
    val options = if (phase == MatchPhase.AUTON) {
        Constants.AUTON_CLIMB_RESULTS
    } else {
        Constants.TELEOP_CLIMB_RESULTS
    }

    LaunchedEffect(result) {
        if (result.isNotEmpty()) {
            onDataChange(ClimbData(result = result, phase = phase))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Climb Result", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { option ->
                    FilterChip(
                        selected = result == option,
                        onClick = { result = option },
                        label = { Text(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// Defense Form
@Composable
fun DefenseForm(
    robotDesignation: String,
    opposingTeams: Map<String, Int> = emptyMap(),
    onDataChange: (DefenseData) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(setOf<String>()) }
    var targetRobot by remember { mutableStateOf("") }

    // Determine opposing alliance robots
    val isRedRobot = robotDesignation.startsWith("Red")
    val opposingRobots = if (isRedRobot) {
        listOf("Blue1", "Blue2", "Blue3")
    } else {
        listOf("Red1", "Red2", "Red3")
    }

    LaunchedEffect(selectedTypes, targetRobot) {
        if (selectedTypes.isNotEmpty() && targetRobot.isNotEmpty()) {
            onDataChange(DefenseData(types = selectedTypes.toList(), targetRobot = targetRobot))
        }
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Target Robot Selection
        Text("Defending Against", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            opposingRobots.forEach { robot ->
                // Show team number if available, otherwise show position
                val teamNum = opposingTeams[robot]
                val label = if (teamNum != null && teamNum > 0) teamNum.toString() else robot
                FilterChip(
                    selected = targetRobot == robot,
                    onClick = { targetRobot = robot },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Defense Type Selection
        Text("Defense Type (select all that apply)", style = MaterialTheme.typography.titleMedium)
        Constants.DEFENSE_TYPES.forEach { type ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = type in selectedTypes,
                    onCheckedChange = { checked ->
                        selectedTypes = if (checked) {
                            selectedTypes + type
                        } else {
                            selectedTypes - type
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(type, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
            }
        }
    }
}

// Foul Form (NO TIMER)
@Composable
fun FoulForm(
    onDataChange: (FoulData) -> Unit
) {
    var foulType by remember { mutableStateOf("") }

    LaunchedEffect(foulType) {
        if (foulType.isNotEmpty()) {
            onDataChange(FoulData(type = foulType))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Foul Type", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Constants.FOUL_TYPES.forEach { type ->
                FilterChip(
                    selected = foulType == type,
                    onClick = { foulType = type },
                    label = { Text(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Damaged Form (NO TIMER)
@Composable
fun DamagedForm(
    onDataChange: (DamagedData) -> Unit
) {
    var selectedComponents by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(selectedComponents) {
        if (selectedComponents.isNotEmpty()) {
            onDataChange(DamagedData(components = selectedComponents.toList()))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Damaged Components (select all that apply)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Constants.DAMAGED_COMPONENTS.forEach { component ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = component in selectedComponents,
                    onCheckedChange = { checked ->
                        selectedComponents = if (checked) {
                            selectedComponents + component
                        } else {
                            selectedComponents - component
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(component, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically))
            }
        }
    }
}

// Simple form for Incapacitated and Tipped (no additional data)
@Composable
fun SimpleForm() {
    Text(
        "Press 'End' when action is complete.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
