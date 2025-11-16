package com.oportunyfam_mobile.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory para criar AtividadeViewModel
 * Nota: AtividadeViewModel não requer parâmetros no construtor
 */
class AtividadeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AtividadeViewModel::class.java)) {
            return AtividadeViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

