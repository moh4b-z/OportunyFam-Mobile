package com.oportunyfam_mobile.Utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0

    companion object {
        private const val TAG = "AudioRecorder"
    }

    /**
     * Inicia a gravação de áudio
     * @return File do áudio gravado ou null em caso de erro
     */
    fun startRecording(): File? {
        try {
            // Criar arquivo temporário para o áudio
            val audioDir = File(context.cacheDir, "audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }

            audioFile = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000)
                    setAudioSamplingRate(44100)
                    setOutputFile(audioFile?.absolutePath)

                    prepare()
                    start()
                    startTime = System.currentTimeMillis()

                    Log.d(TAG, "Gravação iniciada: ${audioFile?.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Erro ao configurar/iniciar gravação", e)
                    release()
                    mediaRecorder = null
                    audioFile = null
                    throw e
                }
            }

            return audioFile
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao iniciar gravação", e)
            // Não chama stopRecording() porque pode não ter sido iniciado
            try {
                mediaRecorder?.release()
            } catch (ex: Exception) {
                Log.e(TAG, "Erro ao liberar MediaRecorder", ex)
            }
            mediaRecorder = null
            audioFile = null
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado ao iniciar gravação", e)
            // Não chama stopRecording() porque pode não ter sido iniciado
            try {
                mediaRecorder?.release()
            } catch (ex: Exception) {
                Log.e(TAG, "Erro ao liberar MediaRecorder", ex)
            }
            mediaRecorder = null
            audioFile = null
            return null
        }
    }

    /**
     * Para a gravação e retorna o arquivo e duração
     * @return Pair<File, Int> com o arquivo e duração em segundos
     */
    fun stopRecording(): Pair<File?, Int> {
        var duration = 0

        try {
            mediaRecorder?.apply {
                stop()
                release()

                // Calcular duração
                val endTime = System.currentTimeMillis()
                duration = ((endTime - startTime) / 1000).toInt()

                Log.d(TAG, "Gravação finalizada. Duração: $duration segundos")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao parar gravação", e)
        } finally {
            mediaRecorder = null
        }

        return Pair(audioFile, duration)
    }

    /**
     * Cancela a gravação e deleta o arquivo
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar gravação", e)
        } finally {
            mediaRecorder = null
            audioFile?.delete()
            audioFile = null
            Log.d(TAG, "Gravação cancelada")
        }
    }

    /**
     * Verifica se está gravando
     */
    fun isRecording(): Boolean {
        return mediaRecorder != null
    }

    /**
     * Obtém a duração atual da gravação em segundos
     */
    fun getCurrentDuration(): Int {
        return if (startTime > 0) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }
    }
}

