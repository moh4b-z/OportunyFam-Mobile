package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategories: List<Int>,
    onCategorySelected: (categoryId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryFilterChip(
                category = category,
                selected = selectedCategories.contains(category.id),
                onSelectionChanged = { onCategorySelected(category.id) }
            )
        }
    }
}

