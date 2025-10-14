package com.example.data

import android.content.Context
import com.example.oportunyfam.model.Instituicao
import com.example.oportunyfam.model.Usuario
import com.example.oportunyfam.model.Crianca
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utilitário para salvar e carregar dados do usuário logado (Usuário ou Criança) usando SharedPreferences.
 * É necessário o Gson no build.gradle: implementation 'com.google.code.gson:gson:2.10.1'
 */
class AuthDataStore(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "OportunyFamAuth"
        // Chaves para os diferentes tipos de usuário
        private const val KEY_INSTITUICAO_JSON = "instituicao_json"
        private const val KEY_USUARIO_JSON = "usuario_json"
        private const val KEY_CRIANCA_JSON = "crianca_json"
        private const val KEY_USER_TYPE = "user_type" // Para diferenciar quem está logado (USUARIO, CRIANCA, INSTITUICAO)
    }

    // =================================================================
    // MÉTODOS DE SALVAMENTO
    // =================================================================

    fun saveUsuario(usuario: Usuario) {
        val json = gson.toJson(usuario)
        sharedPreferences.edit()
            .remove(KEY_INSTITUICAO_JSON)
            .remove(KEY_CRIANCA_JSON)
            .putString(KEY_USUARIO_JSON, json)
            .putString(KEY_USER_TYPE, "USUARIO")
            .apply()
    }

    fun saveCrianca(crianca: Crianca) {
        val json = gson.toJson(crianca)
        sharedPreferences.edit()
            .remove(KEY_INSTITUICAO_JSON)
            .remove(KEY_USUARIO_JSON)
            .putString(KEY_CRIANCA_JSON, json)
            .putString(KEY_USER_TYPE, "CRIANCA")
            .apply()
    }

    // Método de Instituição mantido para compatibilidade, mas removido dos fluxos principais de Responsáveis/Crianças
    fun saveInstituicao(instituicao: Instituicao) {
        val json = gson.toJson(instituicao)
        sharedPreferences.edit()
            .remove(KEY_USUARIO_JSON)
            .remove(KEY_CRIANCA_JSON)
            .putString(KEY_INSTITUICAO_JSON, json)
            .putString(KEY_USER_TYPE, "INSTITUICAO")
            .apply()
    }

    // =================================================================
    // MÉTODOS DE CARREGAMENTO (SUSPEND)
    // =================================================================

    suspend fun loadUsuario(): Usuario? = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_USUARIO_JSON, null) ?: return@withContext null
        return@withContext try {
            gson.fromJson(json, Usuario::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    suspend fun loadCrianca(): Crianca? = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_CRIANCA_JSON, null) ?: return@withContext null
        return@withContext try {
            gson.fromJson(json, Crianca::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    // Método de Instituição mantido
    suspend fun loadInstituicao(): Instituicao? = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_INSTITUICAO_JSON, null) ?: return@withContext null
        return@withContext try {
            gson.fromJson(json, Instituicao::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    // =================================================================
    // MÉTODOS DE VERIFICAÇÃO E LIMPEZA
    // =================================================================

    /**
     * Retorna o tipo de usuário logado ("USUARIO", "CRIANCA", "INSTITUICAO") ou null.
     */
    fun getUserType(): String? {
        return sharedPreferences.getString(KEY_USER_TYPE, null)
    }

    /**
     * Verifica se há algum usuário logado (usuário, criança ou instituição).
     */
    fun isUserLoggedIn(): Boolean {
        // Se qualquer uma das chaves principais de dados estiver presente, consideramos logado
        return sharedPreferences.contains(KEY_USUARIO_JSON) ||
                sharedPreferences.contains(KEY_CRIANCA_JSON) ||
                sharedPreferences.contains(KEY_INSTITUICAO_JSON)
    }

    /**
     * Limpa todos os dados de autenticação.
     */
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}
