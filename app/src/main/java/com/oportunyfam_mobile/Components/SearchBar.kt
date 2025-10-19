package com.oportunyfam_mobile.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

// Esta constante representa a cor Laranja da sua imagem (0xFFFFA000)
private val CustomOrange = Color(0xFFFFA000)

/**
 * Barra de Pesquisa customizada (estilo OutlinedTextField) que se assemelha ao design da imagem.
 *
 * @param query O texto atual da pesquisa.
 * @param onQueryChange Callback chamado quando o texto é alterado.
 * @param onSearch Callback chamado quando a tecla 'Enter' é pressionada (simula a pesquisa).
 * @param onSearchIconClick Callback chamado quando o ícone da Lupa é clicado.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSearchIconClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 1. O OutlinedTextField é o mais adequado para o visual com borda arredondada.
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),

        // Texto de placeholder
        placeholder = { Text("Pesquise aqui") },

        // 2. Ícone da Lupa (Funciona como um botão para navegação ou pesquisa)
        leadingIcon = {
            // Usamos um IconButton para que o ícone seja clicável.
            IconButton(onClick = onSearchIconClick) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Ícone de Pesquisa",
                    tint = CustomOrange // Cor da Lupa (Laranja)
                )
            }
        },

        // 3. Customização de Cores e Formato
        shape = RoundedCornerShape(28.dp), // Valor alto para bordas totalmente arredondadas
        colors = OutlinedTextFieldDefaults.colors(
            // Cor da Borda (Indicador) - a cor Laranja da sua imagem
            focusedBorderColor = CustomOrange,
            unfocusedBorderColor = CustomOrange,

            // Cor de fundo
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,

            // Cor do texto de placeholder
            unfocusedPlaceholderColor = Color.DarkGray,
            focusedPlaceholderColor = Color.DarkGray,

            // Cor do texto
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),

        // 4. Ações do Teclado (Pronto para a pesquisa na API)
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search // Define a ação "Pesquisar" no teclado
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(query) } // Chama o callback de pesquisa quando 'Pesquisar' é apertado
        ),

        singleLine = true
    )
}