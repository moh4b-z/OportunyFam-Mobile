package com.oportunyfam_mobile.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Service.SexoService
import com.oportunyfam_mobile.Service.UsuarioService
import com.oportunyfam_mobile.model.Usuario
import com.oportunyfam_mobile.model.UsuarioRequest
import com.oportunyfam_mobile.model.UsuarioResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.oportunyfam_mobile.R

private const val TIPO_NIVEL_PADRAO = 1
// private const val CPF_LENGTH = 11

/**
 * Valida a data de nascimento e formata para o padrão LocalDate (necessário para UsuarioRequest).
 * Retorna null se for inválido ou se for uma data futura.
 */
fun formatarDataNascimentoParaLocalDate(dataNascimento: String): LocalDate? {
    if (dataNascimento.length != 10) return null // DD/MM/AAAA
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(dataNascimento, inputFormatter)

        // Verifica se a data não é futura
        if (date.isAfter(LocalDate.now())) {
            return null
        }
        return date
    } catch (e: DateTimeParseException) {
        null
    }
}

/**
 * Adaptação do componente de registro para o fluxo de Usuário/Responsável.
 */
@Composable
fun RegistroContent(
    navController: NavHostController?,
    // Dados Pessoais
    nome: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    dataNascimento: MutableState<String>, // DD/MM/AAAA formatado no input
    cpf: MutableState<String>, // CPF limpo (11 dígitos)
    idSexo: MutableState<Int?>, // ID do Sexo selecionado
    selectedSexoName: MutableState<String>, // Nome do Sexo selecionado
    // Endereço
    cep: MutableState<String>, // CEP limpo (8 dígitos)
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
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    usuarioService: UsuarioService, // Serviço para criar Usuário
    sexoService: SexoService, // Serviço para carregar opções de Sexo
    scope: CoroutineScope,
    onAuthSuccess: (Usuario) -> Unit // Recebe Usuario para o callback
) {
    val context = LocalContext.current

    // Estado local para a validação do CPF
    var isCpfInputValid by remember { mutableStateOf(false) }

    // --- CÁLCULO DE VALIDAÇÃO GERAL (Passo Único) ---
    val dataNascLocalDate = formatarDataNascimentoParaLocalDate(dataNascimento.value)
    val isBasicDataValid = nome.value.isNotBlank() && email.value.isNotBlank() && phone.value.isNotBlank()
    val isBirthDateValid = dataNascLocalDate != null
    val isSexoSelected = idSexo.value != null
    val isAddressValid = logradouro.value.isNotBlank() && bairro.value.isNotBlank() && cidade.value.isNotBlank() && estado.value.isNotBlank() && cep.value.length == 8
    val isPasswordMatch = senha.value.length >= 6 && senha.value == confirmarSenha.value
    val isTermsAccepted = concordaTermos.value

    val isFormValid = isBasicDataValid && isCpfInputValid && isBirthDateValid && isSexoSelected && isAddressValid && isPasswordMatch && isTermsAccepted
    // --------------------------------------------------

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ... (Seção de Dados Pessoais)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Nome
            RegistroOutlinedTextField(
                value = nome.value,
                onValueChange = { nome.value = it },
                label = stringResource(R.string.label_name),
                leadingIcon = {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.desc_icon_name),
                        tint = Color(0x9E000000)
                    )
                },
                readOnly = isLoading.value,
                // CORREÇÃO: Adiciona supportingText vazio para evitar erro de parâmetro faltante.
                supportingText = {}
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                // CORREÇÃO: Adiciona supportingText vazio
                supportingText = {}
            )
        }
        item {
            // Telefone
            RegistroOutlinedTextField(
                value = phone.value,
                onValueChange = { phone.value = it.filter { char -> char.isDigit() || char in "()- " } },
                label = stringResource(R.string.label_phone),
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = stringResource(R.string.desc_icon_phone), tint = Color(0x9E000000)) },
                readOnly = isLoading.value,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                // CORREÇÃO: Adiciona supportingText vazio
                supportingText = {}
            )
        }

        item {
            // Linha CPF e Data de Nascimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CPF
                CpfTextField(
                    modifier = Modifier.weight(1f),
                    readOnly = isLoading.value,
                    onValidationSuccess = { cleanCpf ->
                        cpf.value = cleanCpf
                        isCpfInputValid = true
                    }
                )

                // Data de Nascimento
                RegistroOutlinedTextField(
                    value = dataNascimento.value,
                    onValueChange = { newValue ->
                        // Implementação básica de máscara DD/MM/AAAA para o input
                        val cleanValue = newValue.filter { it.isDigit() }.take(8)
                        val maskedValue = when (cleanValue.length) {
                            in 3..4 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2)}"
                            in 5..6 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2, 4)}"
                            in 7..8 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2, 4)}/${cleanValue.substring(4)}"
                            else -> cleanValue
                        }
                        dataNascimento.value = maskedValue
                    },
                    label = "Data de Nascimento (DD/MM/AAAA)",
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Ícone Data", tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    // CORREÇÃO: Adiciona supportingText vazio
                    supportingText = {}
                )
            }
            // Adiciona um Supporting Text para o CPF caso o usuário ainda não tenha validado
            if (cpf.value.length == CPF_LENGTH && !isCpfInputValid && !isLoading.value) {
                Text(
                    text = "Aperte 'Done' no campo CPF para validar o número.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (dataNascimento.value.length == 10 && !isBirthDateValid && !isLoading.value) {
                Text(
                    text = "Data de Nascimento inválida ou futura. Use o formato DD/MM/AAAA.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            // Sexo (Selector Customizado)
            SexoSelector(
                selectedSexoId = idSexo,
                selectedSexoName = selectedSexoName,
                isEnabled = !isLoading.value,
                sexoService = sexoService,
                scope = scope
            )
        }

        // ... (Seção de Endereço)
        item { Spacer(modifier = Modifier.height(10.dp)) }
        item {
            Text(
                text = "Endereço",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            // CEP (COMPONENTE CUSTOMIZADO)
            CepTextField(
                modifier = Modifier.fillMaxWidth(),
                onValidationSuccess = { data: ViaCepData ->
                    cep.value = data.cep.replace("-", "")
                    logradouro.value = data.logradouro
                    bairro.value = data.bairro
                    cidade.value = data.localidade
                    estado.value = data.uf
                    errorMessage.value = null
                }
            )
        }

        // Gatilho visual (os campos de endereço só aparecem se o CEP foi buscado)
        val isAddressInputVisible = cep.value.length == 8

        if (isAddressInputVisible) {
            item {
                // Logradouro (Rua, Avenida)
                RegistroOutlinedTextField(
                    value = logradouro.value,
                    onValueChange = { logradouro.value = it },
                    label = stringResource(R.string.label_street),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.label_street), tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    // CORREÇÃO: Adiciona supportingText vazio
                    supportingText = {}
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        // CORREÇÃO: Adiciona supportingText vazio
                        supportingText = {}
                    )
                    RegistroOutlinedTextField(
                        value = complemento.value,
                        onValueChange = { complemento.value = it },
                        label = stringResource(R.string.label_complement_optional),
                        modifier = Modifier.weight(1f),
                        readOnly = isLoading.value,
                        // CORREÇÃO: Adiciona supportingText vazio
                        supportingText = {}
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
                    readOnly = isLoading.value,
                    // CORREÇÃO: Adiciona supportingText vazio
                    supportingText = {}
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
                        readOnly = isLoading.value,
                        // CORREÇÃO: Adiciona supportingText vazio
                        supportingText = {}
                    )
                    RegistroOutlinedTextField(
                        value = estado.value,
                        onValueChange = { estado.value = it },
                        label = stringResource(R.string.label_state_uf),
                        modifier = Modifier.weight(1f),
                        readOnly = isLoading.value,
                        // CORREÇÃO: Adiciona supportingText vazio
                        supportingText = {}
                    )
                }
            }
        }

        // ... (Seção de Senhas e Termos)
        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            RegistroOutlinedTextField(
                value = senha.value,
                onValueChange = { senha.value = it },
                label = stringResource(R.string.label_password),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
                visualTransformation = PasswordVisualTransformation(),
                readOnly = isLoading.value,
                // CORREÇÃO: Adiciona supportingText vazio
                supportingText = {}
            )
        }
        item {
            RegistroOutlinedTextField(
                value = confirmarSenha.value,
                onValueChange = { confirmarSenha.value = it },
                label = stringResource(R.string.label_confirm_password),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
                visualTransformation = PasswordVisualTransformation(),
                readOnly = isLoading.value,
                // CORREÇÃO: Adiciona supportingText vazio
                supportingText = {}
            )
        }

        item {
            // Termos de Serviço
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
                // Link Termos
                Text(
                    text = stringResource(R.string.label_terms_link),
                    color = PrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !isLoading.value) { /* Ação para ver termos */ }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ... (Seção do Botão de Submissão)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isSubmitEnabled = !isLoading.value && isFormValid

                // Botão Cadastrar
                Button(
                    onClick = {
                        errorMessage.value = null

                        if (!isFormValid) {
                            errorMessage.value = context.getString(R.string.error_fill_all_personal_data)
                            return@Button
                        }

                        // CORREÇÃO: Usa a função adaptada para LocalDate
                        val dataNascimentoLocalDate = formatarDataNascimentoParaLocalDate(dataNascimento.value)
                        if (dataNascimentoLocalDate == null) {
                            errorMessage.value = "Data de Nascimento inválida ou futura. Use o formato DD/MM/AAAA."
                            return@Button
                        }
                        if (idSexo.value == null) {
                            errorMessage.value = "Selecione o Gênero."
                            return@Button
                        }

                        isLoading.value = true
                        scope.launch {
                            try {
                                val request = UsuarioRequest(
                                    nome = nome.value,
                                    email = email.value,
                                    senha = senha.value,
                                    telefone = phone.value.filter { it.isDigit() }.takeIf { it.isNotBlank() },
                                    cpf = cpf.value,
                                    // CORREÇÃO: Passa LocalDate
                                    data_nascimento = dataNascimentoLocalDate,
                                    id_sexo = idSexo.value!!,
                                    id_tipo_nivel = TIPO_NIVEL_PADRAO,
                                    logradouro = logradouro.value,
                                    numero = numero.value.takeIf { it.isNotBlank() },
                                    complemento = complemento.value.takeIf { it.isNotBlank() },
                                    bairro = bairro.value,
                                    cidade = cidade.value,
                                    estado = estado.value,
                                    cep = cep.value,
                                    foto_perfil = ""
                                )

                                // CORREÇÃO: O Retrofit com função 'suspend' retorna Response<T> diretamente
                                // Se o serviço está definido como 'suspend fun criar(req: UsuarioRequest): Response<UsuarioResponse>'
                                // a chamada está correta, mas a atribuição de tipo deve ser Response<UsuarioResponse>.
                                // O erro 'Initializer type mismatch' sugere que o RetrofitService.criar() não é suspend.
                                // ASSUMINDO que você QUER usar 'suspend' e 'await()', mas o erro aponta que você está chamando
                                // uma função NÃO-SUSPEND que retorna Call<T>. Vamos ASSUMIR que 'criar' é SUSPEND.
                                // Se for suspend, a linha original está CORRETA. Se não, você precisa de '.execute()/.await()'.
                                // Vou manter a sintaxe de função SUSPEND, pois é o padrão moderno.
                                // Se o erro persistir, a definição da sua interface UsuarioService é que deve ser revisada
                                // para usar a keyword 'suspend'.

                                val response: Response<UsuarioResponse> = usuarioService.criar(request).execute()

                                if (response.isSuccessful && response.body() != null) {
                                    val usuarioResponse = response.body()!!
                                    // CORREÇÃO: Pega o objeto Usuario de dentro do UsuarioResponse
                                    usuarioResponse.usuario?.let { usuarioCriado ->
                                        onAuthSuccess(usuarioCriado)
                                    } ?: run {
                                        errorMessage.value = context.getString(R.string.error_registration_failed) + "Resposta da API OK, mas objeto usuário nulo."
                                        isLoading.value = false
                                    }

                                } else {
                                    val errorBody = response.errorBody()?.string() ?: response.message()
                                    errorMessage.value = context.getString(R.string.error_registration_failed) + errorBody
                                    isLoading.value = false
                                }
                            } catch (e: Exception) {
                                errorMessage.value = context.getString(R.string.error_connection_failed) + e.message
                                isLoading.value = false
                            }
                        }
                    },
                    enabled = isSubmitEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.button_submit_register),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}