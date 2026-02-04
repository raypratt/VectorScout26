package com.example.vectorscout26.ui.match.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp),
        colors = containerColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp
            )
            if (count > 0) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
