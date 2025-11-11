package com.oportunyfam_mobile.Service

import android.util.Log
import com.oportunyfam_mobile.model.Mensagem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Serviço Firebase para gerenciar mensagens em tempo real
 * Estrutura no Firebase:
 * conversas/
 *   {conversaId}/
 *     mensagens/
 *       {mensagemId}/
 *         - id
 *         - descricao
 *         - visto
 *         - criado_em
 *         - id_conversa
 *         - id_pessoa
 *
 * Configuração do Firebase:
 * - Project ID: oportunyfamong
 * - Database URL: https://oportunyfamong-default-rtdb.firebaseio.com/
 *
 * ⚠️ IMPORTANTE: Ter 2 apps no mesmo projeto Firebase NÃO é problema!
 * O Firebase diferencia pelos package names:
 * - com.oportunyfam_mobile (este app - para responsáveis)
 * - com.oportunyfam_mobile_ong (app para instituições)
 * Ambos compartilham o mesmo Realtime Database e projeto.
 */
class FirebaseMensagemService {
    // ✅ Configuração explícita da URL do Realtime Database
    private val database: DatabaseReference = try {
        FirebaseDatabase.getInstance("https://oportunyfamong-default-rtdb.firebaseio.com/").reference
    } catch (e: Exception) {
        Log.e("FirebaseMensagemService", "⚠️ Falha ao configurar URL customizada, usando padrão", e)
        FirebaseDatabase.getInstance().reference
    }

    private val TAG = "FirebaseMensagemService"

    init {
        // Log para verificar se a configuração está correta
        Log.d(TAG, "🔥 Firebase Database inicializado")
        Log.d(TAG, "📱 Projeto: oportunyfamong")
        Log.d(TAG, "🔗 Database URL: https://oportunyfamong-default-rtdb.firebaseio.com/")
    }

    /**
     * Escuta mensagens em tempo real de uma conversa
     * Retorna um Flow que emite a lista atualizada sempre que há mudanças
     */
    fun observarMensagens(conversaId: Int): Flow<List<Mensagem>> = callbackFlow {
        val conversaRef = database.child("conversas").child(conversaId.toString()).child("mensagens")

        Log.d(TAG, "👂 Iniciando escuta para conversa $conversaId")
        Log.d(TAG, "📍 Path: conversas/$conversaId/mensagens")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mensagens = mutableListOf<Mensagem>()

                Log.d(TAG, "🔥 onDataChange disparado! Snapshot existe: ${snapshot.exists()}, Filhos: ${snapshot.childrenCount}")

                for (mensagemSnapshot in snapshot.children) {
                    try {
                        val mensagem = mensagemSnapshot.getValue(MensagemFirebase::class.java)
                        mensagem?.let {
                            mensagens.add(
                                Mensagem(
                                    id = it.id,
                                    descricao = it.descricao,
                                    visto = it.visto,
                                    criado_em = it.criado_em,
                                    atualizado_em = it.atualizado_em,
                                    id_conversa = it.id_conversa,
                                    id_pessoa = it.id_pessoa
                                )
                            )
                            Log.d(TAG, "  ✅ Mensagem ${it.id}: \"${it.descricao.take(20)}...\"")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erro ao converter mensagem: ${mensagemSnapshot.key}", e)
                    }
                }

                // Ordenar por data de criação
                mensagens.sortBy { it.criado_em }

                Log.d(TAG, "📤 Emitindo ${mensagens.size} mensagens para o Flow")
                trySend(mensagens).isSuccess.also { sucesso ->
                    if (!sucesso) {
                        Log.e(TAG, "❌ Falha ao enviar mensagens para o Flow!")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Erro ao escutar mensagens: ${error.message} (Code: ${error.code})")
                Log.e(TAG, "❌ Detalhes: ${error.details}")
                close(error.toException())
            }
        }

        conversaRef.addValueEventListener(listener)
        Log.d(TAG, "✅ Listener registrado com sucesso para conversa $conversaId")

        awaitClose {
            conversaRef.removeEventListener(listener)
            Log.d(TAG, "👋 Listener removido para conversa $conversaId")
        }
    }

    /**
     * Envia uma nova mensagem para o Firebase
     */
    suspend fun enviarMensagem(mensagem: Mensagem): Result<String> {
        return try {
            val conversaRef = database.child("conversas")
                .child(mensagem.id_conversa.toString())
                .child("mensagens")

            // Usa o ID da mensagem do backend como chave
            val mensagemRef = conversaRef.child(mensagem.id.toString())

            val mensagemFirebase = MensagemFirebase(
                id = mensagem.id,
                descricao = mensagem.descricao,
                visto = mensagem.visto,
                criado_em = mensagem.criado_em,
                atualizado_em = mensagem.atualizado_em,
                id_conversa = mensagem.id_conversa,
                id_pessoa = mensagem.id_pessoa
            )

            mensagemRef.setValue(mensagemFirebase).await()

            Log.d(TAG, "Mensagem enviada para Firebase: ${mensagem.id}")
            Result.success(mensagemRef.key ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar mensagem para Firebase", e)
            Result.failure(e)
        }
    }

    /**
     * Marca uma mensagem como vista no Firebase
     */
    suspend fun marcarComoVista(conversaId: Int, mensagemId: Int): Result<Unit> {
        return try {
            val mensagemRef = database.child("conversas")
                .child(conversaId.toString())
                .child("mensagens")
                .child(mensagemId.toString())

            mensagemRef.child("visto").setValue(true).await()

            Log.d(TAG, "Mensagem $mensagemId marcada como vista")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar mensagem como vista", e)
            Result.failure(e)
        }
    }

    /**
     * Sincroniza mensagens do backend para o Firebase
     * Usado para carregar mensagens iniciais ou resincronizar
     */
    suspend fun sincronizarMensagens(conversaId: Int, mensagens: List<Mensagem>): Result<Unit> {
        return try {
            val conversaRef = database.child("conversas")
                .child(conversaId.toString())
                .child("mensagens")

            // Limpa mensagens antigas
            conversaRef.removeValue().await()

            // Adiciona todas as mensagens
            mensagens.forEach { mensagem ->
                val mensagemFirebase = MensagemFirebase(
                    id = mensagem.id,
                    descricao = mensagem.descricao,
                    visto = mensagem.visto,
                    criado_em = mensagem.criado_em,
                    atualizado_em = mensagem.atualizado_em,
                    id_conversa = mensagem.id_conversa,
                    id_pessoa = mensagem.id_pessoa
                )

                conversaRef.child(mensagem.id.toString()).setValue(mensagemFirebase).await()
            }

            Log.d(TAG, "Sincronizadas ${mensagens.size} mensagens para conversa $conversaId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao sincronizar mensagens", e)
            Result.failure(e)
        }
    }
}

/**
 * Modelo de dados para Firebase (precisa ser compatível com Firebase Realtime Database)
 */
data class MensagemFirebase(
    val id: Int = 0,
    val descricao: String = "",
    val visto: Boolean = false,
    val criado_em: String = "",
    val atualizado_em: String? = null,
    val id_conversa: Int = 0,
    val id_pessoa: Int = 0
)
