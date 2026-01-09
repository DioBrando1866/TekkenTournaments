package com.example.tekkentournaments

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

// --- 1. MODELOS ACTUALIZADOS ---
data class TekkenDocsResponse(
    val character: String?,
    val moves: List<TekkenMove>?
)

data class TekkenMove(
    val name: String?,
    val command: String?,
    val startup: String?,
    val block: String?,
    val hit: String?,
    // Propiedades extra (basadas en lo que suele devolver la API o para futura expansión)
    val counterHit: String? = null,
    val notes: String? = null
)

// --- 2. SERVICIO DE RED ---
interface TekkenApiService {
    @GET("api/t8/{characterName}/framedata")
    suspend fun getCharacterFrameData(@Path("characterName") name: String): TekkenDocsResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://tekkendocs.com/"

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Android Studio)")
                    .build()
                chain.proceed(request)
            }
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    val api: TekkenApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TekkenApiService::class.java)
    }
}

// --- 3. PANTALLA PRINCIPAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameDataScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCharacterMoves by remember { mutableStateOf<List<TekkenMove>?>(null) }
    var selectedName by remember { mutableStateOf("") }
    var isDownloading by remember { mutableStateOf(false) }

    val characters = listOf(
        "Alisa", "Anna", "Armor King", "Asuka", "Bryan", "Claudio", "Clive", "Dragunov",
        "Fahkumram", "Feng", "Heihachi", "Hwoarang", "Jack-8", "Jin", "Jun", "Kazuya",
        "King", "Kuma", "Lars", "Law", "Lee", "Leo", "Leroy", "Lidia", "Lili", "Nina",
        "Panda", "Paul", "Raven", "Reina", "Shaheen", "Steve", "Victor", "Xiaoyu",
        "Yoshimitsu", "Zafina"
    )

    val specialSlugs = mapOf(
        "Armor King" to "armor-king",
        "Jack-8" to "jack-8",
        "Lidia" to "lidia-sobieska",
        "Heihachi" to "heihachi-mishima"
    )

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("FRAME DATA", fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = Color(0xFFD32F2F)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1E1E1E))
                )

                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar luchador...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFD32F2F)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD32F2F),
                            unfocusedBorderColor = Color(0xFF333333),
                            cursorColor = Color(0xFFD32F2F)
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(
                    Brush.horizontalGradient(colors = listOf(Color(0xFFD32F2F), Color(0xFF1E1E1E)))
                ))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val filteredCharacters = characters.filter { it.contains(searchQuery, ignoreCase = true) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCharacters) { name ->
                    CharacterItem(name) {
                        scope.launch {
                            isDownloading = true
                            selectedName = name
                            try {
                                val slug = specialSlugs[name] ?: name.lowercase().replace(" ", "-")
                                val response = RetrofitClient.api.getCharacterFrameData(slug)
                                if (response.moves != null) {
                                    selectedCharacterMoves = response.moves
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                            } finally {
                                isDownloading = false
                            }
                        }
                    }
                }
            }

            if (isDownloading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                }
            }

            if (selectedCharacterMoves != null) {
                MovesDetailDialog(
                    characterName = selectedName,
                    moves = selectedCharacterMoves!!,
                    onDismiss = { selectedCharacterMoves = null }
                )
            }
        }
    }
}

@Composable
fun CharacterItem(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(name.uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("DETALLES", color = Color(0xFFD32F2F), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesDetailDialog(characterName: String, moves: List<TekkenMove>, onDismiss: () -> Unit) {
    var moveFilter by remember { mutableStateOf("") }
    var activeTagFilter by remember { mutableStateOf("TODOS") }

    // Lógica de Filtrado Avanzada
    val filteredMoves = remember(moveFilter, activeTagFilter, moves) {
        moves.filter { move ->
            val matchesText = (move.command?.contains(moveFilter, ignoreCase = true) ?: false) ||
                    (move.name?.contains(moveFilter, ignoreCase = true) ?: false)

            val matchesTag = when(activeTagFilter) {
                "SAFE" -> {
                    val blockValue = move.block?.filter { it.isDigit() || it == '-' || it == '+' }?.toIntOrNull()
                    blockValue != null && blockValue >= -9
                }
                "LAUNCH" -> move.hit?.contains("Launch", ignoreCase = true) == true ||
                        move.hit?.contains("KND", ignoreCase = true) == true
                "PLUS" -> move.block?.startsWith("+") == true
                else -> true
            }
            matchesText && matchesTag
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
            Column {
                Column(modifier = Modifier.background(Color(0xFF1E1E1E))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(characterName.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                    }

                    // Buscador
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        TextField(
                            value = moveFilter,
                            onValueChange = { moveFilter = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Filtrar por comando o nombre...", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF252525),
                                unfocusedContainerColor = Color(0xFF252525),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // --- IDEA 2: FILTROS RÁPIDOS (CHIPS) ---
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tags = listOf("TODOS", "SAFE", "LAUNCH", "PLUS")
                        items(tags) { tag ->
                            FilterChip(
                                selected = activeTagFilter == tag,
                                onClick = { activeTagFilter = tag },
                                label = { Text(tag, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD32F2F),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color.Gray,
                                    containerColor = Color(0xFF252525)
                                )
                            )
                        }
                    }
                    Divider(color = Color(0xFFD32F2F), thickness = 2.dp)
                }

                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(filteredMoves) { move ->
                        MoveRowEnhanced(move)
                        HorizontalDivider(color = Color(0xFF333333), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// --- IDEA 1: PROPIEDADES Y ETIQUETAS VISUALES ---
@Composable
fun MoveRowEnhanced(move: TekkenMove) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(move.name ?: "Move", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.weight(1f))

            // Etiquetas automáticas basadas en datos
            if (move.hit?.contains("Launch", ignoreCase = true) == true) {
                PropertyTag("LAUNCH", Color(0xFFD32F2F))
            }
            if (move.block?.startsWith("+") == true) {
                PropertyTag("PLUS", Color.Green)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(move.command ?: "???", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Row {
                FrameVal("START", move.startup ?: "--")
                Spacer(Modifier.width(16.dp))
                val b = move.block ?: "--"
                val bCol = if (b.startsWith("+")) Color.Green
                else if (b.contains("-") && (b.filter { it.isDigit() }.toIntOrNull() ?: 0) >= 10) Color(0xFFD32F2F)
                else Color.White
                FrameVal("BLOCK", b, bCol)
            }
        }
    }
}

@Composable
fun PropertyTag(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun FrameVal(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 9.sp)
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}