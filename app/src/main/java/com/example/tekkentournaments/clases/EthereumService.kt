package com.example.tekkentournaments.clases

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.* // ✅ Importante para construir JSONs complejos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger

@Serializable
data class EthRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonElement,
    val id: Int = 1
)

@Serializable
data class EthRpcResponse(
    val jsonrpc: String = "2.0",
    val id: Int? = null,
    val result: String? = null,
    val error: EthRpcError? = null
)

@Serializable
data class EthRpcError(
    val code: Int,
    val message: String
)

object EthereumService {

    private const val TICKET_CONTRACT_ADDRESS = "0xd9145CCE52D386f254917e481eB44e9943F39138"

    private val RPC_URLS = listOf(
        "https://sepolia.drpc.org",
        "https://ethereum-sepolia.publicnode.com",
        "https://1rpc.io/sepolia",
        "https://rpc.sepolia.org"
    )

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
        }
    }

    suspend fun obtenerSaldoEth(walletAddress: String): String {
        return withContext(Dispatchers.IO) {
            if (!walletAddress.startsWith("0x") || walletAddress.length != 42) {
                return@withContext "Dirección inválida"
            }

            val params = buildJsonArray {
                add(walletAddress)
                add("latest")
            }

            val request = EthRpcRequest(method = "eth_getBalance", params = params)
            val hex = ejecutarLlamadaRpc(request) ?: return@withContext "Err Red"

            if (hex == "0x" || hex == "0" || hex.isEmpty()) return@withContext "0.0000 SEP"

            try {
                val cleanHex = hex.removePrefix("0x")
                val wei = BigInteger(cleanHex, 16)
                val divisor = BigDecimal("1000000000000000000")
                val ether = BigDecimal(wei).divide(divisor)
                return@withContext "${ether.setScale(4, BigDecimal.ROUND_HALF_UP)} SEP"
            } catch (e: Exception) {
                return@withContext "Err Formato"
            }
        }
    }

    suspend fun tieneEntradaNFT(walletAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (walletAddress.length != 42) return@withContext false

            val functionSelector = "f6a3d24e"

            val cleanAddress = walletAddress.removePrefix("0x")
            val paddedAddress = cleanAddress.padStart(64, '0')

            val data = "0x$functionSelector$paddedAddress"

            val params = buildJsonArray {
                add(buildJsonObject {
                    put("to", TICKET_CONTRACT_ADDRESS)
                    put("data", data)
                })
                add("latest")
            }

            val request = EthRpcRequest(method = "eth_call", params = params)

            val resultHex = ejecutarLlamadaRpc(request) ?: return@withContext false

            try {
                val hasTicket = BigInteger(resultHex.removePrefix("0x"), 16)
                return@withContext hasTicket > BigInteger.ZERO
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }

    private suspend fun ejecutarLlamadaRpc(request: EthRpcRequest): String? {
        for (url in RPC_URLS) {
            try {
                Log.d("CRYPTO", "Probando nodo: $url")

                val response = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                if (response.status == HttpStatusCode.OK) {
                    val rpcResponse = response.body<EthRpcResponse>()

                    if (rpcResponse.error != null) {
                        Log.w("CRYPTO", "Error lógico del nodo: ${rpcResponse.error.message}")
                        continue
                    }

                    if (rpcResponse.result != null) {
                        return rpcResponse.result
                    }
                }
            } catch (e: Exception) {
                Log.e("CRYPTO", "Fallo nodo $url: ${e.message}")
            }
        }
        return null
    }
}