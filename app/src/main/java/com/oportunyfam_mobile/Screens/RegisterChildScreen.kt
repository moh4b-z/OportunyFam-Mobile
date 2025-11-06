package com.oportunyfam_mobile.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import java.util.regex.Pattern
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.navigation.NavHostController

data class ChildRegistration(
    val name: String,
    val age: Int?,
    val cpf: String,
    val sex: String,
    val email: String,
    val password: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterChildScreen(
    modifier: Modifier = Modifier,
    onSubmit: (ChildRegistration) -> Unit = {},
    navController: NavHostController? = null
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }

    // Error states
    var nameError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var cpfError by remember { mutableStateOf(false) }
    var sexError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val sexOptions = listOf("Masculino", "Feminino", "Outro", "Prefiro não dizer")

    fun resetErrors() {
        nameError = false
        ageError = false
        cpfError = false
        sexError = false
        emailError = false
        passwordError = false
    }

    fun validateAndSubmit() {
        resetErrors()
        var valid = true

        if (name.trim().length < 2) {
            nameError = true
            valid = false
        }

        val ageInt = ageText.toIntOrNull()
        if (ageInt == null || ageInt <= 0 || ageInt > 120) {
            ageError = true
            valid = false
        }

        val cpfDigits = cpf.filter { it.isDigit() }
        if (cpfDigits.length != 11) {
            cpfError = true
            valid = false
        }

        if (sex.isBlank()) {
            sexError = true
            valid = false
        }

        if (!isValidEmail(email)) {
            emailError = true
            valid = false
        }

        if (password.length < 6) {
            passwordError = true
            valid = false
        }

        if (valid) {
            val registration = ChildRegistration(
                name = name.trim(),
                age = ageInt,
                cpf = cpfDigits,
                sex = sex,
                email = email.trim(),
                password = password
            )
            onSubmit(registration)
            Toast.makeText(context, "Cadastro do filho realizado!", Toast.LENGTH_SHORT).show()
            // opcional: limpar campos após submit
            name = ""
            ageText = ""
            cpf = ""
            sex = ""
            email = ""
            password = ""
        } else {
            Toast.makeText(context, "Corrija os campos destacados.", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController?.navigate("home") }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color(0xFF424242)
                    )
                }

                Text(
                    text = "Cadastrar Filho",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome completo") },
                isError = nameError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) Text("Nome inválido (min 2 caracteres)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ageText,
                onValueChange = { new -> ageText = new.filter { it.isDigit() } }, // aceita só dígitos
                label = { Text("Idade") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ageError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (ageError) Text("Idade inválida", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cpf,
                onValueChange = {
                    // mantem só digitos e aplica máscara simples enquanto digita
                    val digits = it.filter { ch -> ch.isDigit() }.take(11)
                    cpf = formatCpf(digits)
                },
                label = { Text("CPF") },
                isError = cpfError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = VisualTransformation.None,
                modifier = Modifier.fillMaxWidth()
            )
            if (cpfError) Text("CPF deve ter 11 dígitos", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = sexExpanded,
                onExpandedChange = { sexExpanded = !sexExpanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = sex,
                    onValueChange = {},
                    label = { Text("Sexo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                    isError = sexError,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sexExpanded,
                    onDismissRequest = { sexExpanded = false }
                ) {
                    sexOptions.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                sex = selection
                                sexExpanded = false
                            }
                        )
                    }
                }
            }
            if (sexError) Text("Selecione uma opção", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError) Text("E-mail inválido", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Criar senha") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                isError = passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            if (passwordError) Text("Senha muito curta (mín 6 caracteres)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { validateAndSubmit() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFAF25), // cor de fundo (laranja)
                    contentColor = Color.White          // cor do texto
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Cadastrar Filho")
            }
        }
    }
}

/** Simple CPF masker: 000.000.000-00 */
private fun formatCpf(digits: String): String {
    val d = digits.filter { it.isDigit() }
    val sb = StringBuilder()
    for (i in d.indices) {
        sb.append(d[i])
        if (i == 2 || i == 5) sb.append('.')
        if (i == 8) sb.append('-')
    }
    return sb.toString()
}

private fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    return pattern.matcher(email).matches()
}

@Preview(showBackground = true)
@Composable
private fun PreviewRegisterChild() {
    MaterialTheme {
        RegisterChildScreen(onSubmit = { /*preview*/ })
    }
}
