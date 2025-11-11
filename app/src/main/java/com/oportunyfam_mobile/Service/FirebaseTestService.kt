package com.oportunyfam_mobile_ong.Service

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

/**
 * Classe de teste simples para verificar se o Firebase está funcionando
 * Use esta classe para testar se as dependências do Firebase estão corretas
 */
class FirebaseTestService {

    fun testarConexaoFirebase(): Boolean {
        return try {
            val database = FirebaseDatabase.getInstance()
            val reference = database.reference

            Log.d("FirebaseTest", "✅ Firebase Database inicializado com sucesso!")
            Log.d("FirebaseTest", "📱 Referência: $reference")

            true
        } catch (e: Exception) {
            Log.e("FirebaseTest", "❌ Erro ao inicializar Firebase Database", e)
            false
        }
    }

    fun testarEscritaSimples() {
        try {
            val database = FirebaseDatabase.getInstance()
            val testRef = database.reference.child("test")

            testRef.setValue("Hello Firebase!")

            Log.d("FirebaseTest", "✅ Teste de escrita executado!")
        } catch (e: Exception) {
            Log.e("FirebaseTest", "❌ Erro no teste de escrita", e)
        }
    }
}
