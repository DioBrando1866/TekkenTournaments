package com.example.tekkentournaments.clases

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal

object EthereumService {

    // 1. Nos conectamos a un Nodo Público de Sepolia (Red de pruebas)
    // En producción usarías Infura o Alchemy, pero esto sirve para clase.
    private const val SEPOLIA_RPC_URL = "https://rpc.sepolia.org"

    private val web3j: Web3j by lazy {
        Web3j.build(HttpService(SEPOLIA_RPC_URL))
    }

    // Función para ver el saldo de una cuenta (Ej: La del ganador)
    suspend fun obtenerSaldoEth(walletAddress: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Validación básica de dirección ETH (debe empezar por 0x y tener 42 chars)
                if (!walletAddress.startsWith("0x") || walletAddress.length != 42) {
                    return@withContext "Dirección inválida"
                }

                // Llamada a la Blockchain (ethGetBalance)
                val ethGetBalance = web3j
                    .ethGetBalance(walletAddress, org.web3j.protocol.core.DefaultBlockParameterName.LATEST)
                    .send()

                val wei = ethGetBalance.balance

                // Convertir Wei (unidad mínima) a Ether
                val ether = Convert.fromWei(wei.toString(), Convert.Unit.ETHER)

                // Formatear a 4 decimales
                return@withContext "${ether.setScale(4, BigDecimal.ROUND_HALF_UP)} SEP"

            } catch (e: Exception) {
                Log.e("CRYPTO", "Error leyendo blockchain: ${e.message}")
                return@withContext "Error Red"
            }
        }
    }
}