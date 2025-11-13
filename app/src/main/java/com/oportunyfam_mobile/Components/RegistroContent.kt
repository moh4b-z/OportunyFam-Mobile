package com.oportunyfam_mobile.Components

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.UsuarioService
import com.oportunyfam_mobile.Utils.convertDataParaBackendFormat
import com.oportunyfam_mobile.Utils.formatarDataNascimentoParaLocalDate
import com.oportunyfam_mobile.model.Usuario
import com.oportunyfam_mobile.model.UsuarioRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Constantes de controle de etapa
private const val STEP_DADOS_PESSOAIS = 1
private const val STEP_CONTATO_E_ENDERECO = 2
private const val STEP_SENHAS_E_TERMOS = 3
private const val TIPO_NIVEL_PADRAO = 1

@Composable
fun RegistroContent(
    navController: NavHostController?,
    // Dados Pessoais
    nome: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    dataNascimento: MutableState<String>,
    cpf: MutableState<String>,
    idSexo: MutableState<Int?>,
    selectedSexoName: MutableState<String>,
    // Endereço
    cep: MutableState<String>,
    logradouro: MutableState<String>,
    numero: MutableState<String>,
    complemento: MutableState<String>,
    bairro: MutableState<String>,
    cidade: MutableState<String>,
    estado: MutableState<String>,
    // Senhas
    senha: MutableState<String>,
    confirmarSenha: MutableState<String>,
    concordaTermos: MutableState<Boolean>,
    // Controles
    currentStep: MutableState<Int>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    usuarioService: UsuarioService,
    scope: CoroutineScope,
    onAuthSuccess: (Usuario) -> Unit
) {
    val context = LocalContext.current

    // Estado para controlar o scroll da tela
    val scrollState = rememberLazyListState()

    // Estado local para a validação do CPF
    var isCpfInputValid by remember { mutableStateOf(false) }

    // Variáveis de validação
    val dataNascLocalDate = formatarDataNascimentoParaLocalDate(dataNascimento.value)
    val isBirthDateValid = dataNascLocalDate != null
    val isSexoSelected = idSexo.value != null

    // Validações de Endereço
    // Complemento é opcional, mas Logradouro, Bairro, Cidade, Estado e Número são obrigatórios.
    val isAddressValid = cep.value.length == 8 && logradouro.value.isNotBlank() && bairro.value.isNotBlank() && cidade.value.isNotBlank() && estado.value.isNotBlank() && numero.value.isNotBlank()

    // Etapa 1: Nome, Email, Sexo, CPF (TODOS OBRIGATÓRIOS)
    val isStep1Valid = nome.value.isNotBlank() && email.value.isNotBlank() && isCpfInputValid && isSexoSelected

    // Etapa 2: Telefone, Data de Nascimento, Endereço completo (TODOS OBRIGATÓRIOS)
    val isStep2Valid = phone.value.isNotBlank() && isBirthDateValid && isAddressValid


    // Etapa 3: Senhas e Termos
    val isPasswordMatch = senha.value.length >= 6 && senha.value == confirmarSenha.value
    val isStep3Valid = isPasswordMatch && concordaTermos.value

    // Efeito para avançar automaticamente quando o CPF é validado na etapa 1
    LaunchedEffect(isCpfInputValid, currentStep.value) {
        if (isCpfInputValid && isStep1Valid && currentStep.value == STEP_DADOS_PESSOAIS) {
            // Aguarda um pouco para garantir que a validação foi processada
            kotlinx.coroutines.delay(300)
            currentStep.value = STEP_CONTATO_E_ENDERECO
        }
    }

    // Efeito para fazer scroll suave ao topo quando muda de etapa
    LaunchedEffect(currentStep.value) {
        scrollState.animateScrollToItem(0)
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // --- ETAPA 1: DADOS PESSOAIS ---
        if (currentStep.value == STEP_DADOS_PESSOAIS) {
            item {
                RegistroOutlinedTextField(
                    value = nome.value,
                    onValueChange = { nome.value = it },
                    label = stringResource(R.string.label_name),
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    supportingText = { Text(stringResource(R.string.label_name)) }
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = stringResource(R.string.label_email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    supportingText = { Text(stringResource(R.string.label_email)) }
                )
            }
            item {
                SexoSelector(
                    selectedSexoId = idSexo,
                    selectedSexoName = selectedSexoName,
                    isEnabled = !isLoading.value
                )
            }
            // *** CAMPO CPF REINTRODUZIDO E OBRIGATÓRIO PARA AVANÇAR ***
            item {
                Column {
                    CpfTextField(
                        readOnly = isLoading.value,
                        onValidationSuccess = { cleanCpf ->
                            cpf.value = cleanCpf
                            isCpfInputValid = true
                        }
                    )
                    if (cpf.value.length == 11 && !isCpfInputValid && !isLoading.value) {
                        Text(
                            text = "Aperte 'Done' no campo CPF para validar o número.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            item {
                // Controles de Navegação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNextEnabled = !isLoading.value && isStep1Valid

                    Row(
                        modifier = Modifier
                            .clickable(enabled = isNextEnabled) {
                                if (isStep1Valid) {
                                    errorMessage.value = null
                                    currentStep.value = STEP_CONTATO_E_ENDERECO
                                } else {
                                    errorMessage.value = context.getString(R.string.error_fill_all_personal_data)
                                }
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.button_next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNextEnabled) PrimaryColor else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.button_next), tint = if (isNextEnabled) PrimaryColor else Color.LightGray)
                    }
                }
            }
        }

        // --- ETAPA 2: CONTATO E ENDEREÇO COMPLETO ---
        if (currentStep.value == STEP_CONTATO_E_ENDERECO) {
            item {
                RegistroOutlinedTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it.filter { char -> char.isDigit() || char in "()- " } },
                    label = stringResource(R.string.label_phone),
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    supportingText = { Text(stringResource(R.string.label_phone)) }
                )
            }
            // --- CAMPO DE DATA DE NASCIMENTO (APENAS DATE PICKER) ---
            item {
                val context = LocalContext.current
                val calendar = remember { java.util.Calendar.getInstance() }

                var dataNascimentoValue by remember { mutableStateOf(dataNascimento.value) }

                val datePickerDialog = remember {
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                            dataNascimentoValue = selectedDate
                            dataNascimento.value = selectedDate
                        },
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH),
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                }

                Column {
                    RegistroOutlinedTextField(
                        value = dataNascimentoValue,
                        onValueChange = {}, // Não permite digitar nada
                        label = "Data de Nascimento (DD/MM/AAAA)",
                        leadingIcon = {
                            IconButton(
                                onClick = { datePickerDialog.show() }
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "Selecionar data",
                                    tint = Color(0x9E000000)
                                )
                            }
                        },
                        readOnly = true, // Campo só leitura
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {}
                    )

                    if (dataNascimento.value.length == 10 && !isBirthDateValid && !isLoading.value) {
                        Text(
                            text = "Data inválida (formato ou futura). Use DD/MM/AAAA.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            // --- CAMPOS DE ENDEREÇO (Movidos para a Etapa 2) ---
            item {
                // 1. CEP (COMPONENTE CUSTOMIZADO)
                CepTextField(
                    modifier = Modifier.fillMaxWidth(),
                    onValidationSuccess = { data: ViaCepData ->
                        // Atualiza os estados principais do formulário com os dados da API
                        cep.value = data.cep.replace("-", "") // Salva o CEP limpo
                        logradouro.value = data.logradouro
                        bairro.value = data.bairro
                        cidade.value = data.localidade
                        estado.value = data.uf
                        errorMessage.value = null
                    }
                )
            }

            // Gatilho visual de que o CEP já foi digitado e processado
            val isAddressInputVisible = cep.value.isNotBlank() && cep.value.length == 8

            if (isAddressInputVisible) {
                item {
                    // Logradouro (Rua, Avenida) - Preenchido automaticamente, mas editável
                    RegistroOutlinedTextField(
                        value = logradouro.value,
                        onValueChange = { logradouro.value = it },
                        label = stringResource(R.string.label_street),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.label_street), tint = Color(0x9E000000)) },
                        readOnly = isLoading.value,
                        supportingText = { Text(stringResource(R.string.label_street)) }
                    )
                }

                item {
                    // Número/Complemento (Número é OBRIGATÓRIO, Complemento Opcional)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = numero.value,
                            onValueChange = { numero.value = it },
                            label = stringResource(R.string.label_number), // Alterado para indicar que é obrigatório
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            supportingText = { Text(stringResource(R.string.label_number)) }
                        )
                        RegistroOutlinedTextField(
                            value = complemento.value,
                            onValueChange = { complemento.value = it },
                            label = stringResource(R.string.label_complement_optional),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value,
                            supportingText = { Text(stringResource(R.string.label_complement_optional)) }
                        )
                    }
                }

                item {
                    // Bairro - Preenchido automaticamente
                    RegistroOutlinedTextField(
                        value = bairro.value,
                        onValueChange = { bairro.value = it },
                        label = stringResource(R.string.label_neighborhood),
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = stringResource(R.string.label_neighborhood), tint = Color(0x9E000000)) },
                        readOnly = true, // Mantido como ReadOnly pois vem do CEP
                        supportingText = { Text(stringResource(R.string.label_neighborhood)) }
                    )
                }

                item {
                    // Cidade/Estado - Preenchido automaticamente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = cidade.value,
                            onValueChange = { cidade.value = it },
                            label = stringResource(R.string.label_city),
                            modifier = Modifier.weight(1f),
                            readOnly = true, // Mantido como ReadOnly pois vem do CEP
                            supportingText = { Text(stringResource(R.string.label_city)) }
                        )
                        RegistroOutlinedTextField(
                            value = estado.value,
                            onValueChange = { estado.value = it },
                            label = stringResource(R.string.label_state_uf),
                            modifier = Modifier.weight(1f),
                            readOnly = true, // Mantido como ReadOnly pois vem do CEP
                            supportingText = { Text(stringResource(R.string.label_state_uf)) }
                        )
                    }
                }
            }


            item {
                // Controles de Navegação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Voltar
                    Row(
                        modifier = Modifier.clickable(enabled = !isLoading.value) {
                            errorMessage.value = null
                            currentStep.value = STEP_DADOS_PESSOAIS
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.button_back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }

                    val isNextEnabled = !isLoading.value && isStep2Valid

                    // Botão Próximo
                    Row(
                        modifier = Modifier
                            .clickable(enabled = isNextEnabled) {
                                if (isStep2Valid) {
                                    errorMessage.value = null
                                    currentStep.value = STEP_SENHAS_E_TERMOS
                                } else {
                                    errorMessage.value = "Preencha todos os campos obrigatórios (Telefone, Data, Endereço completo, incluindo o Número) e valide a data de nascimento."
                                }
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.button_next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNextEnabled) PrimaryColor else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.button_next), tint = if (isNextEnabled) PrimaryColor else Color.LightGray)
                    }
                }
            }
        }

        // --- ETAPA 3: SENHAS E TERMOS ---
        if (currentStep.value == STEP_SENHAS_E_TERMOS) {
            item {
                RegistroOutlinedTextField(
                    value = senha.value,
                    onValueChange = { senha.value = it },
                    label = stringResource(R.string.label_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value,
                    supportingText = {
                        if (senha.value.length > 0 && senha.value.length < 6) {
                            Text(
                                "A senha deve ter no mínimo 6 caracteres.",
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(stringResource(R.string.label_password))
                        }
                    }
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = confirmarSenha.value,
                    onValueChange = { confirmarSenha.value = it },
                    label = stringResource(R.string.label_confirm_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value,
                    supportingText = {
                        if (confirmarSenha.value.isNotEmpty() && senha.value != confirmarSenha.value) {
                            Text("As senhas não coincidem.", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(stringResource(R.string.label_confirm_password))
                        }
                    }
                )
            }

            item {
                // Termos de Serviço
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = !isLoading.value) {
                            concordaTermos.value = !concordaTermos.value
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = concordaTermos.value,
                        onCheckedChange = { concordaTermos.value = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryColor),
                        enabled = !isLoading.value
                    )
                    Text(
                        text = stringResource(R.string.label_terms_agreement),
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.label_terms_link),
                        color = PrimaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable(enabled = !isLoading.value) { /* Ação para ver termos */ }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                // Controles de Navegação (Submissão)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Voltar
                    Row(
                        modifier = Modifier.clickable(enabled = !isLoading.value) {
                            errorMessage.value = null
                            currentStep.value = STEP_CONTATO_E_ENDERECO
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.button_back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }

                    val isSubmitEnabled = !isLoading.value && isStep3Valid

                    // Botão Cadastrar (Final)
                    Row(
                        modifier = Modifier
                            .clickable(enabled = isSubmitEnabled) {
                                errorMessage.value = null

                                if (!isSubmitEnabled) {
                                    errorMessage.value = "Preencha e confirme as senhas (mín. 6 caracteres) e aceite os termos."
                                    return@clickable
                                }

                                // Lógica de Submissão Integrada
                                isLoading.value = true

                                // Converter a data para o formato YYYY-MM-DD exigido pelo backend
                                val dataNascBackend = convertDataParaBackendFormat(dataNascimento.value)

                                scope.launch {
                                    try {
                                        val request = UsuarioRequest(
                                            nome = nome.value,
                                            email = email.value,
                                            senha = senha.value,
                                            telefone = phone.value.filter { it.isDigit() }.takeIf { it.isNotBlank() },
                                            cpf = cpf.value,
                                            // *** CORREÇÃO: Passando a String no formato AAAA-MM-DD ***
                                            data_nascimento = dataNascBackend,

                                            id_sexo = idSexo.value!!,
                                            id_tipo_nivel = TIPO_NIVEL_PADRAO,
                                            logradouro = logradouro.value,
                                            // Numero é obrigatório
                                            numero = numero.value.takeIf { it.isNotBlank() },
                                            // Complemento é opcional
                                            complemento = complemento.value.takeIf { it.isNotBlank() },
                                            bairro = bairro.value,
                                            cidade = cidade.value,
                                            estado = estado.value,
                                            cep = cep.value,
                                            foto_perfil = ""
                                        )

                                        // Executa a chamada na thread de I/O para não bloquear a Main Thread
                                        val response = withContext(Dispatchers.IO) {
                                            usuarioService.criar(request).execute()
                                        }

                                        if (response.isSuccessful && response.body() != null) {
                                            val usuarioResponse = response.body()!!
                                            usuarioResponse.usuario?.let { usuarioCriado ->
                                                onAuthSuccess(usuarioCriado) // Callback para a tela principal
                                            } ?: run {
                                                Log.e("REGISTRO_API", "Resposta de sucesso, mas objeto usuário nulo no body.")
                                                errorMessage.value = context.getString(R.string.error_registration_failed) + ". Falha ao processar os dados."
                                                isLoading.value = false
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string()
                                            Log.e("REGISTRO_API", "Erro HTTP ${response.code()} (${response.message()}). Body: $errorBody")
                                            errorMessage.value = context.getString(R.string.error_registration_failed) + ". Por favor, verifique seus dados (CPF/Email duplicado) e tente novamente."
                                            isLoading.value = false
                                        }
                                    } catch (e: Exception) {
                                        Log.e("REGISTRO_API", "Erro de conexão/IO/Parsing: ${e.message}", e)
                                        errorMessage.value = context.getString(R.string.error_connection_failed) + ". Verifique sua conexão com a internet."
                                        isLoading.value = false
                                    }
                                }
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                color = PrimaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.button_submit_register),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubmitEnabled) PrimaryColor else Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        item {
            // Exibir erro genérico
            if (!errorMessage.value.isNullOrBlank()) {
                Text(
                    text = errorMessage.value!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
