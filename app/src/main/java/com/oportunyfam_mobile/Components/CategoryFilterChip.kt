package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Category(
    val id: Int,
    val name: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChip(
    category: Category,
    selected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectionChanged(!selected) },
        label = { Text(category.name) },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selecionado",
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (selected) category.color else Color(0xFFF0F0F0),
            labelColor = if (selected) Color.White else Color.Black,
            selectedContainerColor = category.color,
            selectedLabelColor = Color.White
        ),
        modifier = modifier.padding(horizontal = 4.dp)
    )
}

