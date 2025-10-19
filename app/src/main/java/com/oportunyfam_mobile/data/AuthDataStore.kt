package com.oportunyfam_mobile.data

import android.content.Context
import androidx.room.*
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Usuario
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Define os tipos de usuário para segurança de tipo (Enum).
 */
enum class AuthType(val value: String) {
    USUARIO("usuario"),
    CRIANCA("crianca")
}

/**
 * Wrapper para retornar o usuário autenticado e seu tipo.
 */
data class AuthUserWrapper(
    val user: Any, // O objeto Usuario ou Crianca
    val type: AuthType // "usuario" ou "crianca"
)

// =================================================================
// 1. ENTIDADE ROOM (SQLite Table)
// =================================================================

/**
 * Entidade única para armazenar o estado de autenticação.
 * Usamos 'userJson' para armazenar o objeto serializado.
 */
@Entity(tableName = "auth_state")
data class AuthEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // ID fixo para garantir que haja apenas uma linha de estado de login
    @ColumnInfo(name = "user_json")
    val userJson: String,
    @ColumnInfo(name = "user_type")
    val userType: String // Salva o valor da string do AuthType
)

// =================================================================
// 2. DATA ACCESS OBJECT (DAO)
// =================================================================

@Dao
interface AuthDao {
    /**
     * Insere ou substitui o estado de autenticação.
     * Como o id é sempre 1, ele substitui o registro anterior (Upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuth(entity: AuthEntity)

    /**
     * Busca o estado de autenticação (a única linha com ID = 1).
     */
    @Query("SELECT * FROM auth_state WHERE id = 1")
    suspend fun getAuth(): AuthEntity?

    /**
     * Limpa o estado de autenticação (simplesmente deleta a linha).
     */
    @Query("DELETE FROM auth_state")
    suspend fun clearAuth()
}

// =================================================================
// 3. DATABASE ROOM
// =================================================================

@Database(entities = [AuthEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authDao(): AuthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "oportunyfam_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// =================================================================
// 4. DATA STORE (Lógica de Persistência com Room)
// =================================================================

/**
 * Utilitário para salvar e carregar dados do usuário (Responsável ou Criança) logado
 * usando a biblioteca Room (SQLite).
 */
class AuthDataStore(context: Context) {

    // Inicializa o DAO através da instância única do banco de dados
    private val authDao = AppDatabase.getDatabase(context).authDao()
    private val gson = Gson()

    /**
     * Salva o objeto do usuário (Responsável ou Criança) e seu tipo no banco de dados.
     * O salvamento é feito de forma síncrona/segura em uma thread de I/O.
     */
    suspend fun saveAuthUser(user: Any, type: AuthType) = withContext(Dispatchers.IO) {
        val json = gson.toJson(user)
        val entity = AuthEntity(
            userJson = json,
            userType = type.value
        )
        authDao.insertAuth(entity)
    }

    /**
     * Carrega o objeto do usuário e seu tipo do banco de dados.
     */
    suspend fun loadAuthUser(): AuthUserWrapper? = withContext(Dispatchers.IO) {
        val authEntity = authDao.getAuth() ?: return@withContext null

        val json = authEntity.userJson
        val typeString = authEntity.userType

        val type = AuthType.values().find { it.value == typeString } ?: run {
            // Tipo inválido, limpa e retorna nulo (segurança)
            authDao.clearAuth()
            return@withContext null
        }

        return@withContext try {
            // Deserializa o JSON para a classe correta baseada no 'type'
            val userObject: Any? = when (type) {
                AuthType.USUARIO -> gson.fromJson(json, Usuario::class.java)
                AuthType.CRIANCA -> gson.fromJson(json, Crianca::class.java)
            }

            if (userObject != null) AuthUserWrapper(userObject, type) else null

        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            authDao.clearAuth() // Limpa dados corrompidos
            null
        }
    }

    /**
     * Verifica se há um usuário (Responsável ou Criança) logado.
     * Nota: Esta verificação ainda exige uma chamada ao banco de dados,
     * portanto, ela deve ser uma função suspensa (suspend).
     * @return true se houver dados salvos, false caso contrário.
     */
    suspend fun isUserLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        return@withContext authDao.getAuth() != null
    }

    /**
     * Limpa todos os dados de autenticação.
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        authDao.clearAuth()
    }
}