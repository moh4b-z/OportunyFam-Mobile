package com.oportunyfam_mobile.Service

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

/**
 * Interceptor que re-tenta requisições em falhas de I/O ou quando o servidor retorna 502/503/504.
 * Implementa backoff exponencial com jitter.
 *
 * Parâmetros:
 *  - maxRetries: número máximo de tentativas (tentativa inicial + retries = maxRetries + 1)
 *  - initialDelayMillis: delay inicial em ms para o primeiro retry
 *  - maxDelayMillis: delay máximo em ms
 *  - retryNonIdempotentMethods: se true, também irá retentar POST/ PATCH etc (não recomendado por padrão)
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMillis: Long = 1000L,
    private val maxDelayMillis: Long = 10000L,
    private val retryNonIdempotentMethods: Boolean = false
) : Interceptor {

    private val idempotentMethods = setOf("GET", "HEAD", "OPTIONS", "PUT", "DELETE")

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0

        val request = chain.request()
        val method = request.method.uppercase()
        val canRetryMethod = retryNonIdempotentMethods || idempotentMethods.contains(method)

        while (true) {
            try {
                val response = chain.proceed(request)

                // Se for erro de servidor transitório, possivelmente retentar
                if ((response.code == 502 || response.code == 503 || response.code == 504) && attempt < maxRetries && canRetryMethod) {
                    response.close()
                    attempt++
                    val delay = computeDelay(attempt)
                    try {
                        Thread.sleep(delay)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("Interrupted while waiting to retry", ie)
                    }
                    continue
                }

                // Retorna resposta (sucesso ou não retryable)
                return response
            } catch (ioEx: IOException) {
                // Se o método não pode ser retentado, propaga a exceção imediatamente
                if (!canRetryMethod) throw ioEx

                if (attempt >= maxRetries) {
                    // esgotou as tentativas
                    throw ioEx
                }
                attempt++
                val delay = computeDelay(attempt)
                try {
                    Thread.sleep(delay)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted while waiting to retry", ie)
                }
                // e tenta novamente
            }
        }
    }

    private fun computeDelay(attempt: Int): Long {
        // attempt começa em 1 para o primeiro retry
        val cappedShift = (attempt - 1).coerceAtMost(30)
        val exp = initialDelayMillis * (1L shl cappedShift)
        val base = min(exp, maxDelayMillis)
        // jitter entre base/2 e base
        val minJitter = (base / 2).coerceAtLeast(0L)
        return if (base <= minJitter) base else Random.nextLong(minJitter, base + 1)
    }
}
