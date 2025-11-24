package com.oportunyfam_mobile.Components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.model.Usuario
import java.util.regex.Pattern
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.CriancaRequest
import kotlinx.coroutines.launch
import java.util.Locale

data class ChildRegistration(
    val name: String,
    val data_nascimento: String,
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
    // Use TextFieldValue to control cursor/selection during mask insertion
    var dataNascimentoValue by remember { mutableStateOf(TextFieldValue(text = "")) }
    var cpf by remember { mutableStateOf("") }
    var isCpfValidated by remember { mutableStateOf(false) }
    var sex by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val sexOptions = listOf("Masculino", "Feminino", "Outro", "Prefiro não dizer")

    // dataNascimento input uses Brazilian format DD/MM/YYYY; validate and convert before submit
    val datePatternInput = Pattern.compile("\\d{2}/\\d{2}/\\d{4}")
    val isDataValida = dataNascimentoValue.text.isNotBlank() && datePatternInput.matcher(dataNascimentoValue.text).matches()

    val focusManager = LocalFocusManager.current

    val isStep1Valid = name.isNotBlank() && email.isNotBlank() && isCpfValidated && sex.isNotBlank() && isDataValida
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
                // Convert DD/MM/YYYY -> YYYY-MM-DD with semantic validation
                val parts = dataNascimentoValue.text.trim().split('/')
                if (parts.size != 3) {
                    Toast.makeText(context, "Data de nascimento inválida.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }
                val dia = parts[0].toIntOrNull()
                val mes = parts[1].toIntOrNull()
                val ano = parts[2].toIntOrNull()
                if (dia == null || mes == null || ano == null) {
                    Toast.makeText(context, "Data de nascimento inválida.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }
                // Validate ranges
                if (ano < 1900 || ano > 2100 || mes !in 1..12) {
                    Toast.makeText(context, "Data de nascimento inválida.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }
                val maxDay = when (mes) {
                    1,3,5,7,8,10,12 -> 31
                    4,6,9,11 -> 30
                    2 -> if ((ano % 4 == 0 && ano % 100 != 0) || (ano % 400 == 0)) 29 else 28
                    else -> 31
                }
                if (dia < 1 || dia > maxDay) {
                    Toast.makeText(context, "Data de nascimento inválida.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }

                val dataNascimento = String.format(Locale.US, "%04d-%02d-%02d", ano, mes, dia)

                // Converter sexo para ID
                val idSexo = when (sex) {
                    "Masculino" -> 1
                    "Feminino" -> 2
                    "Outro" -> 3
                    else -> 4
                }

                // Get id_usuario from AuthDataStore (responsável que está cadastrando)
                val authWrapper = withContext(Dispatchers.IO) { AuthDataStore(context).loadAuthUser() }
                val idUsuario = (authWrapper?.user as? Usuario)?.usuario_id ?: 0

                val criancaRequest = CriancaRequest(
                    nome = name.trim(),
                    email = email.trim(),
                    senha = password,
                    cpf = cpf,
                    data_nascimento = dataNascimento,
                    id_sexo = idSexo,
                    foto_perfil = "",
                    id_usuario = idUsuario
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
                            data_nascimento = dataNascimento,
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
                        dataNascimentoValue = TextFieldValue("")
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                supportingText = { Text("Nome completo") }
            )
        }

        // Data de nascimento (DD/MM/YYYY) - masked input
        item {
            // Data: use explicit OutlinedTextField with TextFieldValue to keep cursor position
            OutlinedTextField(
                value = dataNascimentoValue,
                onValueChange = { newValue ->
                    // Allow only digits, auto-insert '/'
                    val raw = newValue.text
                    val digits = raw.filter { it.isDigit() }
                    val limited = if (digits.length > 8) digits.substring(0, 8) else digits
                    val sb = StringBuilder()
                    for (i in limited.indices) {
                        sb.append(limited[i])
                        if (i == 1 || i == 3) sb.append('/')
                    }
                    val formatted = sb.toString()
                    // Place cursor at end so the next digit appears after the slash correctly
                    dataNascimentoValue = TextFieldValue(text = formatted, selection = androidx.compose.ui.text.TextRange(formatted.length))
                },
                label = { Text("Data de nascimento (DD/MM/YYYY)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0x9E000000)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Data de nascimento") }
            )
            if (dataNascimentoValue.text.isNotBlank() && !isDataValida) {
                Text(text = "Formato inválido. Use DD/MM/YYYY", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp))
            }
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
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (isFormValid) submitForm() }),
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
                            text = "Cadastrar Criança",
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
