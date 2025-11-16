package com.oportunyfam_mobile.Service

import android.util.Log
import com.oportunyfam_mobile.model.Mensagem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.toString


class FirebaseMensagemService {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val TAG = "FirebaseMensagemService"

    init {
        // Log para verificar se a configuração está correta
        Log.d(TAG, "🔥 Firebase Database URL: ${FirebaseDatabase.getInstance().reference}")
        Log.d(TAG, "📱 Firebase configurado para projeto: oportunyfamong")
    }

    // dentro de FirebaseMensagemService.kt
    data class FirebaseEvent(val type: String, val mensagem: Mensagem)

    fun observarMensagensEventos(conversaId: Int) = callbackFlow {
        val ref = database
            .child("conversas")
            .child(conversaId.toString())
            .child("mensagens")

        val listener = object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mf = snapshot.getValue(MensagemFirebase::class.java) ?: return
                trySend(FirebaseEvent("added", mf.toMensagem()))
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val mf = snapshot.getValue(MensagemFirebase::class.java) ?: return
                trySend(FirebaseEvent("changed", mf.toMensagem()))
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val mf = snapshot.getValue(MensagemFirebase::class.java) ?: return
                trySend(FirebaseEvent("removed", mf.toMensagem()))
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { /* raramente usado */ }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addChildEventListener(listener)
        awaitClose {
            ref.removeEventListener(listener)
        }
    }

    // Helper para converter MensagemFirebase -> Mensagem
    private fun MensagemFirebase.toMensagem() = Mensagem(
        id = id,
        descricao = descricao,
        visto = visto,
        criado_em = criado_em,
        atualizado_em = atualizado_em,
        id_conversa = id_conversa,
        id_pessoa = id_pessoa
    )


    /**
     * Envia uma mensagem para o Firebase
     * Retorna Result<Unit> indicando sucesso ou falha
     */
    suspend fun enviarMensagem(mensagem: Mensagem): Result<Unit> {
        return try {
            val mensagemRef = database.child("conversas")
                .child(mensagem.id_conversa.toString())
                .child("mensagens")
                .child(mensagem.id.toString())

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
            Log.d(TAG, "✅ Mensagem ${mensagem.id} enviada para Firebase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao enviar mensagem para Firebase", e)
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

            // Constrói um map de updates { "id": MensagemFirebase }
            val updates = mutableMapOf<String, Any?>()
            mensagens.forEach { m ->
                updates[m.id.toString()] = MensagemFirebase(
                    id = m.id,
                    descricao = m.descricao,
                    visto = m.visto,
                    criado_em = m.criado_em,
                    atualizado_em = m.atualizado_em,
                    id_conversa = m.id_conversa,
                    id_pessoa = m.id_pessoa
                )
            }

            // Atualiza os nós sem remover outros
            conversaRef.updateChildren(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
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
