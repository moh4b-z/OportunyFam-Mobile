package com.oportunyfam_mobile.Components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.CriancaRequest
import com.oportunyfam_mobile.Utils.convertDataParaBackendFormat
import kotlinx.coroutines.launch
import java.util.Calendar

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
fun ChildRegistrationForm(
    modifier: Modifier = Modifier,
    onSubmit: (ChildRegistration) -> Unit = {},
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    navController: NavController? = null
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var isCpfValidated by remember { mutableStateOf(false) }
    var sex by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val sexOptions = listOf("Masculino", "Feminino", "Outro", "Prefiro não dizer")

    val isStep1Valid = name.isNotBlank() && email.isNotBlank() && isCpfValidated && sex.isNotBlank()
    val isPasswordValid = password.length >= 6
    val isFormValid = isStep1Valid && isPasswordValid

    fun submitForm() {
        if (!isFormValid) {
            Toast.makeText(context, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        coroutineScope.launch {
            try {
                val ageInt = ageText.toIntOrNull() ?: 0
                // Converter idade para data de nascimento aproximada
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.YEAR, -ageInt)
                val ano = calendar.get(Calendar.YEAR)
                val mes = calendar.get(Calendar.MONTH) + 1
                val dia = calendar.get(Calendar.DAY_OF_MONTH)
                val dataNascimento = String.format("%04d-%02d-%02d", ano, mes, dia)

                // Converter sexo para ID
                val idSexo = when (sex) {
                    "Masculino" -> 1
                    "Feminino" -> 2
                    "Outro" -> 3
                    else -> 4
                }

                val criancaRequest = CriancaRequest(
                    nome = name.trim(),
                    email = email.trim(),
                    senha = password,
                    cpf = cpf,
                    data_nascimento = dataNascimento,
                    id_sexo = idSexo,
                    foto_perfil = ""
                )

                val criancaService = RetrofitFactory().getCriancaService()
                android.util.Log.d("ChildReg", "Enviando: $criancaRequest")

                val response = withContext(Dispatchers.IO) {
                    criancaService.criar(criancaRequest).execute()
                }

                android.util.Log.d("ChildReg", "Resposta: Code=${response.code()}, Success=${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val crianca = response.body()!!.crianca
                    if (crianca != null) {
                        android.util.Log.d("ChildReg", "✓ Criança criada: ${crianca.nome}")
                        onSubmit(ChildRegistration(
                            name = name.trim(),
                            age = ageInt,
                            cpf = cpf,
                            sex = sex,
                            email = email.trim(),
                            password = password
                        ))

                        Toast.makeText(context, "✓ Cadastro realizado!", Toast.LENGTH_LONG).show()

                        // Navega direto pra Home se tudo deu certo
                        navController?.navigate("HomeScreen") {
                            popUpTo("child_register") { inclusive = true }
                        }

                        // Limpar campos
                        name = ""
                        ageText = ""
                        cpf = ""
                        isCpfValidated = false
                        sex = ""
                        email = ""
                        password = ""
                    } else {
                        android.util.Log.e("ChildReg", "Criança nula")
                        Toast.makeText(context, " Erro: Resposta vazia", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    android.util.Log.e("ChildReg", "Erro ${response.code()}: $errorBody")
                    Toast.makeText(context, " Erro ${response.code()}: Verifique CPF/Email", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ChildReg", "Exceção: ${e.message}", e)
                Toast.makeText(context, " Erro: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Nome
        item {
            RegistroOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nome completo",
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0x9E000000)) },
                readOnly = false,
                supportingText = { Text("Nome completo") }
            )
        }

        // Idade
        item {
            RegistroOutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it.filter { char -> char.isDigit() } },
                label = "Idade",
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0x9E000000)) },
                readOnly = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Idade") }
            )
        }

        // CPF com validação igual ao Registro
        item {
            Column {
                CpfTextField(
                    readOnly = false,
                    onValidationSuccess = { cleanCpf ->
                        cpf = cleanCpf
                        isCpfValidated = true
                    }
                )
                if (cpf.length == 11 && !isCpfValidated) {
                    Text(
                        text = "Aperte 'Done' no campo CPF para validar o número.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }

        // Sexo
        item {
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
        }

        // Email
        item {
            RegistroOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0x9E000000)) },
                readOnly = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                supportingText = { Text("E-mail") }
            )
        }

        // Senha com visualização
        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Criar senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0x9E000000)) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha",
                            tint = Color(0x9E000000)
                        )
                    }
                },
                visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (password.length > 0 && password.length < 6) {
                        Text("A senha deve ter no mínimo 6 caracteres.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Criar senha")
                    }
                }
            )
        }

        // Botão Cadastrar Centralizado
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                val isSubmitEnabled = isFormValid && !isLoading

                Button(
                    onClick = { if (isFormValid) submitForm() },
                    enabled = isSubmitEnabled,
                    modifier = Modifier
                        .height(48.dp)
                        .width(200.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Cadastrar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadastrar Filho",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
