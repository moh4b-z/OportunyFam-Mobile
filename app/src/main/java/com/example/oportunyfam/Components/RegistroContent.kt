package com.example.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.model.InstituicaoRequest
import com.example.oportunyfam.Service.InstituicaoService
import com.example.oportunyfam.model.Instituicao
import com.example.screens.PrimaryColor
import com.example.screens.RegistroOutlinedTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.oportunyfam_mobile_ong.R

// REMOVIDO: import com.example.data.AuthDataStore

/**
 * Verifica se todos os campos obrigatórios do Passo 1 estão preenchidos.
 */
fun isStep1Valid(nome: String, email: String, phone: String, selectedTypes: List<Int>, cnpj: String): Boolean {
    // Note: cnpj.length == 14 já é feito na validação do componente CNPJ
    return nome.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && selectedTypes.isNotEmpty() && cnpj.length == 14
}

/**
 * Verifica se todos os campos obrigatórios (Endereço, Senhas e Termos) do Passo 2 estão válidos.
 * Esta função agora é apenas para referência externa, o cálculo principal será feito internamente no Composable.
 */
fun isStep2Valid(
    logradouro: String, bairro: String, cidade: String, estado: String,
    senha: String, confirmarSenha: String, concordaTermos: Boolean
): Boolean {
    val isAddressValid = logradouro.isNotBlank() && bairro.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()
    val isPasswordValid = senha.isNotBlank() && confirmarSenha.isNotBlank() && senha == confirmarSenha
    return isAddressValid && isPasswordValid && concordaTermos
}

// =================================================================
// COMPOSABLE PRINCIPAL
// =================================================================

@Composable
fun RegistroContent(
    navController: NavHostController?,
    // Passo 1
    nome: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    selectedTypeIds: MutableState<List<Int>>,
    selectedTypeNames: MutableState<String>,
    cnpj: MutableState<String>,
    // Passo 2
    cep: MutableState<String>,
    logradouro: MutableState<String>,
    numero: MutableState<String>,
    complemento: MutableState<String>,
    bairro: MutableState<String>,
    cidade: MutableState<String>,
    estado: MutableState<String>,
    senha: MutableState<String>,
    confirmarSenha: MutableState<String>,
    concordaTermos: MutableState<Boolean>,
    // Controles
    currentStep: MutableState<Int>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    instituicaoService: InstituicaoService,
    scope: CoroutineScope,
    // REMOVIDO: authDataStore: AuthDataStore
    // ✨ NOVO: Adicionado o callback que a tela principal irá usar para salvar e navegar
    onAuthSuccess: (Instituicao) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ... (ITENS DO PASSO 1 - Mantidos)
        if (currentStep.value == 1) {
            item {
                // Nome
                RegistroOutlinedTextField(
                    value = nome.value,
                    onValueChange = { nome.value = it },
                    label = stringResource(R.string.label_institution_name),
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.desc_icon_name),
                            tint = Color(0x9E000000))
                    },
                    readOnly = isLoading.value
                )
            }
            item {
                // Email
                RegistroOutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = stringResource(R.string.label_email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.desc_icon_email), tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            item {
                // Telefone
                RegistroOutlinedTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it },
                    label = stringResource(R.string.label_phone),
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = stringResource(R.string.desc_icon_phone), tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            item {
                // Tipo de Instituição (Selector customizado)
                TipoInstituicaoSelector(
                    selectedTypeIds = selectedTypeIds,
                    selectedTypeNames = selectedTypeNames,
                    isEnabled = !isLoading.value
                )
            }
            item {
                // CNPJ (COMPONENTE CUSTOMIZADO)
                CnpjTextField(
                    modifier = Modifier.fillMaxWidth(),
                    onValidationSuccess = { cleanCnpj ->
                        cnpj.value = cleanCnpj
                    }
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            // Controles de Navegação do Passo 1
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.button_back),
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    val isNextEnabled = !isLoading.value && isStep1Valid(nome.value, email.value, phone.value, selectedTypeIds.value, cnpj.value)

                    Row(
                        modifier = Modifier
                            .clickable(enabled = isNextEnabled) {
                                if (isStep1Valid(nome.value, email.value, phone.value, selectedTypeIds.value, cnpj.value)) {
                                    errorMessage.value = null
                                    currentStep.value = 2
                                } else {
                                    errorMessage.value = context.getString(R.string.error_fill_all_step_1)
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

        // =================================================================
        // ITENS DO PASSO 2 (Mantidos)
        // =================================================================
        if (currentStep.value == 2) {
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
            val isAddressInputVisible = cep.value.isNotBlank()

            if (isAddressInputVisible) {
                item {
                    // Logradouro (Rua, Avenida)
                    RegistroOutlinedTextField(
                        value = logradouro.value,
                        onValueChange = { logradouro.value = it },
                        label = stringResource(R.string.label_street),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.label_street), tint = Color(0x9E000000)) },
                        readOnly = isLoading.value
                    )
                }

                item {
                    // Número/Complemento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = numero.value,
                            onValueChange = { numero.value = it },
                            label = stringResource(R.string.label_number_optional),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        RegistroOutlinedTextField(
                            value = complemento.value,
                            onValueChange = { complemento.value = it },
                            label = stringResource(R.string.label_complement_optional),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value
                        )
                    }
                }

                item {
                    // Bairro
                    RegistroOutlinedTextField(
                        value = bairro.value,
                        onValueChange = { bairro.value = it },
                        label = stringResource(R.string.label_neighborhood),
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = stringResource(R.string.label_neighborhood), tint = Color(0x9E000000)) },
                        readOnly = isLoading.value
                    )
                }

                item {
                    // Cidade/Estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = cidade.value,
                            onValueChange = { cidade.value = it },
                            label = stringResource(R.string.label_city),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value
                        )
                        RegistroOutlinedTextField(
                            value = estado.value,
                            onValueChange = { estado.value = it },
                            label = stringResource(R.string.label_state_uf),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value
                        )
                    }
                }
            }

            // 3. Senhas
            item {
                RegistroOutlinedTextField(
                    value = senha.value,
                    onValueChange = { senha.value = it },
                    label = stringResource(R.string.label_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = confirmarSenha.value,
                    onValueChange = { confirmarSenha.value = it },
                    label = stringResource(R.string.label_confirm_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value
                )
            }

            // 4. Termos de Serviço
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                        modifier = Modifier.clickable(enabled = !isLoading.value) { /* Ação para ver termos */ }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 5. Controles de Navegação do Passo 2 (Cadastrar)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Voltar (Ativo na Etapa 2)
                    Row(
                        modifier = Modifier.clickable(enabled = !isLoading.value) {
                            errorMessage.value = null
                            currentStep.value = 1
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

                    // --- CÁLCULO DE VALIDAÇÃO REVISADO (Mantido) ---
                    val isAddressValid = logradouro.value.isNotBlank() && bairro.value.isNotBlank() && cidade.value.isNotBlank() && estado.value.isNotBlank()
                    val isPasswordMatch = senha.value.isNotBlank() && confirmarSenha.value.isNotBlank() && senha.value == confirmarSenha.value
                    val isTermsAccepted = concordaTermos.value

                    val isStep2FormValid = isAddressValid && isPasswordMatch && isTermsAccepted

                    val isSubmitEnabled = !isLoading.value && isAddressInputVisible && isStep2FormValid
                    // ------------------------------------

                    // Botão Cadastrar
                    Row(
                        modifier = Modifier
                            .clickable(enabled = isSubmitEnabled) {
                                errorMessage.value = null

                                if (!isSubmitEnabled) {
                                    when {
                                        !isAddressInputVisible -> errorMessage.value = context.getString(R.string.error_fill_all_step_2)
                                        !isAddressValid -> errorMessage.value = "Preencha o endereço (Logradouro/Bairro/Cidade/Estado)."
                                        !isPasswordMatch -> errorMessage.value = context.getString(R.string.error_password_mismatch)
                                        !isTermsAccepted -> errorMessage.value = context.getString(R.string.error_accept_terms)
                                        else -> errorMessage.value = context.getString(R.string.error_fill_all_step_2)
                                    }
                                    return@clickable
                                }

                                if (selectedTypeIds.value.isEmpty()) {
                                    errorMessage.value = context.getString(R.string.error_select_institution_type)
                                    return@clickable
                                }

                                isLoading.value = true
                                scope.launch {
                                    try {
                                        val request = InstituicaoRequest(
                                            nome = nome.value,
                                            logo = null,
                                            cnpj = cnpj.value,
                                            email = email.value,
                                            senha = senha.value,
                                            descricao = null,
                                            telefone = phone.value.takeIf { it.isNotBlank() },
                                            tipos_instituicao = selectedTypeIds.value,
                                            logradouro = logradouro.value,
                                            numero = numero.value.takeIf { it.isNotBlank() },
                                            complemento = complemento.value.takeIf { it.isNotBlank() },
                                            bairro = bairro.value,
                                            cidade = cidade.value,
                                            estado = estado.value,
                                            cep = cep.value
                                        )

                                        val response: Response<Instituicao> = instituicaoService.criar(request)

                                        if (response.isSuccessful && response.body() != null) {
                                            val instituicaoCriada = response.body()!!

                                            // ✨ CHAMADA AO CALLBACK CENTRALIZADO
                                            // A responsabilidade de salvar e navegar está agora em RegistroScreen.kt
                                            onAuthSuccess(instituicaoCriada)

                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: response.message()
                                            errorMessage.value = context.getString(R.string.error_registration_failed) + errorBody
                                            isLoading.value = false // Para o loading em caso de erro
                                        }
                                    } catch (e: Exception) {
                                        errorMessage.value = context.getString(R.string.error_connection_failed) + e.message
                                        isLoading.value = false // Para o loading em caso de exceção
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
    }
}