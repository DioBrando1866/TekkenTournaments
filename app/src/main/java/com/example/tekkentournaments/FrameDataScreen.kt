package com.example.tekkentournaments

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.annotations.SerializedName
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

// --- MODELOS ---
data class TekkenDocsResponse(
    @SerializedName("characterName") val character: String? = null,
    @SerializedName("framesNormal") val moves: List<TekkenMove>? = null
)

data class TekkenMove(
    val name: String? = null,
    val command: String? = null,
    val startup: String? = null,
    val block: String? = null,
    val hit: String? = null,
    val tags: Map<String, String>? = null
)

// --- API ---
interface TekkenApiService {
    @GET("api/t8/{characterName}/framedata")
    suspend fun getCharacterFrameData(@Path("characterName") name: String): TekkenDocsResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://tekkendocs.com/"
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val sslContext = SSLContext.getInstance("SSL").apply { init(null, trustAllCerts, SecureRandom()) }
        return OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }.build()
    }
    val api: TekkenApiService by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create()).build().create(TekkenApiService::class.java)
    }
}

// --- VISUALIZACIÓN ---

enum class ControlMode { TEXT, PS, XB }

@Composable
fun VisualCommand(command: String, mode: ControlMode, useArrows: Boolean) {
    // Definición de colores oficiales corregidos
    val colorPS_Square = Color(0xFFF48FB1) // Rosa
    val colorPS_Triangle = Color(0xFF4CAF50) // Verde
    val colorPS_X = Color(0xFF2196F3) // Azul
    val colorPS_Circle = Color(0xFFF44336) // Rojo

    val colorXB_X = Color(0xFF2196F3) // Azul
    val colorXB_Y = Color(0xFFFBC02D) // Amarillo
    val colorXB_A = Color(0xFF4CAF50) // Verde
    val colorXB_B = Color(0xFFF44336) // Rojo

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        val parts = command.split("(?<=[+,])|(?=[+,])".toRegex())

        parts.forEach { part ->
            val clean = part.trim().lowercase()
            when {
                useArrows && clean == "f" -> ArrowIcon(0f)
                useArrows && clean == "b" -> ArrowIcon(180f)
                useArrows && clean == "u" -> ArrowIcon(270f)
                useArrows && clean == "d" -> ArrowIcon(90f)
                useArrows && clean == "df" -> ArrowIcon(45f)
                useArrows && clean == "db" -> ArrowIcon(135f)
                useArrows && clean == "uf" -> ArrowIcon(315f)
                useArrows && clean == "ub" -> ArrowIcon(225f)

                clean.contains("1") && mode != ControlMode.TEXT -> {
                    val color = if (mode == ControlMode.PS) colorPS_Square else colorXB_X
                    ButtonCircle(if (mode == ControlMode.PS) "□" else "X", color)
                }
                clean.contains("2") && mode != ControlMode.TEXT -> {
                    val color = if (mode == ControlMode.PS) colorPS_Triangle else colorXB_Y
                    ButtonCircle(if (mode == ControlMode.PS) "△" else "Y", color)
                }
                clean.contains("3") && mode != ControlMode.TEXT -> {
                    val color = if (mode == ControlMode.PS) colorPS_X else colorXB_A
                    ButtonCircle(if (mode == ControlMode.PS) "✕" else "A", color)
                }
                clean.contains("4") && mode != ControlMode.TEXT -> {
                    val color = if (mode == ControlMode.PS) colorPS_Circle else colorXB_B
                    ButtonCircle(if (mode == ControlMode.PS) "○" else "B", color)
                }

                clean == "+" -> Text(" + ", color = Color.Gray, fontWeight = FontWeight.Bold)
                clean == "," -> Text(", ", color = Color.Gray)

                else -> Text(
                    text = part.uppercase(),
                    color = if (part.any { it.isDigit() }) Color(0xFFD32F2F) else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ArrowIcon(degrees: Float) {
    Icon(Icons.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(22.dp).rotate(degrees).padding(horizontal = 2.dp))
}

@Composable
fun ButtonCircle(symbol: String, color: Color) {
    // Diseño imitando un botón real: Fondo negro, símbolo y borde de color
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(24.dp) // Un poco más grande para el borde
            .background(Color.Black, CircleShape)
            .border(1.5.dp, color, CircleShape)
    ) {
        Text(
            text = symbol,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.offset(y = (-1).dp)
        )
    }
}

// --- UI ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameDataScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedMoves by remember { mutableStateOf<List<TekkenMove>?>(null) }
    var charName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val chars = listOf("Alisa", "Asuka", "Bryan", "Claudio", "Dragunov", "Feng", "Heihachi", "Hwoarang", "Jack-8", "Jin", "Jun", "Kazuya", "King", "Kuma", "Lars", "Law", "Lee", "Leo", "Leroy", "Lidia", "Lili", "Nina", "Panda", "Paul", "Raven", "Reina", "Shaheen", "Steve", "Victor", "Xiaoyu", "Yoshimitsu", "Zafina")

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("FRAME DATA", fontWeight = FontWeight.Black, color = Color(0xFFD32F2F)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(16.dp)) {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Buscar luchador...", color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFD32F2F)) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                }
            }
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            val filtered = chars.filter { it.contains(searchQuery, ignoreCase = true) }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { name ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            loading = true
                            charName = name
                            try {
                                val res = RetrofitClient.api.getCharacterFrameData(name.lowercase().replace(" ", ""))
                                selectedMoves = res.moves
                            } catch (e: Exception) { Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show() }
                            finally { loading = false }
                        }
                    }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
                        Text(name.uppercase(), modifier = Modifier.padding(20.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFFD32F2F))
            if (selectedMoves != null) {
                MovesDetailDialog(charName, selectedMoves!!, { selectedMoves = null })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovesDetailDialog(characterName: String, moves: List<TekkenMove>, onDismiss: () -> Unit) {
    var moveSearch by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("TODOS") }
    var controlMode by remember { mutableStateOf(ControlMode.TEXT) }
    var useArrows by remember { mutableStateOf(false) }

    val filteredMoves = remember(moveSearch, activeFilter, moves) {
        moves.filter { move ->
            val matchesText = move.command?.contains(moveSearch, ignoreCase = true) == true ||
                    move.name?.contains(moveSearch, ignoreCase = true) == true
            val matchesFilter = when (activeFilter) {
                "SAFE" -> (move.block?.filter { it.isDigit() || it == '-' }?.toIntOrNull() ?: -99) >= -9
                "LAUNCH" -> move.hit?.contains("Launch", ignoreCase = true) == true
                "HEAT" -> move.tags?.containsKey("hb") == true || move.tags?.containsKey("he") == true
                "POWER CRUSH" -> move.tags?.containsKey("pc") == true
                else -> true
            }
            matchesText && matchesFilter
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
            Column {
                Column(modifier = Modifier.background(Color(0xFF1E1E1E)).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(characterName.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                    }
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendItem(Icons.Rounded.Whatshot, "HB", Color(0xFFD32F2F))
                        LegendItem(Icons.Rounded.Bolt, "HE", Color(0xFFD32F2F))
                        LegendItem(Icons.Rounded.Shield, "PC", Color(0xFF2196F3))
                        LegendItem(Icons.Rounded.Radar, "HOM", Color(0xFFFFEB3B))
                        LegendItem(Icons.Rounded.Tornado, "TRN", Color(0xFF00BCD4))
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.background(Color.Black, RoundedCornerShape(20.dp)).padding(4.dp)) {
                            listOf(ControlMode.TEXT, ControlMode.PS, ControlMode.XB).forEach { mode ->
                                Box(modifier = Modifier.background(if (controlMode == mode) Color(0xFFD32F2F) else Color.Transparent, RoundedCornerShape(16.dp)).clickable { controlMode = mode }.padding(horizontal = 12.dp, vertical = 2.dp)) {
                                    Text(mode.name, color = if (controlMode == mode) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { useArrows = !useArrows }) {
                            Checkbox(checked = useArrows, onCheckedChange = { useArrows = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F)))
                            Text("FLECHAS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(value = moveSearch, onValueChange = { moveSearch = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Filtrar...") }, shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    LazyRow(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf("TODOS", "SAFE", "LAUNCH", "HEAT", "POWER CRUSH")
                        items(filters) { f ->
                            FilterChip(selected = activeFilter == f, onClick = { activeFilter = f }, label = { Text(f, fontSize = 10.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFD32F2F), selectedLabelColor = Color.White))
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    items(filteredMoves) { move ->
                        MoveRowEnhanced(move, controlMode, useArrows)
                        HorizontalDivider(color = Color(0xFF333333))
                    }
                }
            }
        }
    }
}

@Composable
fun MoveRowEnhanced(move: TekkenMove, mode: ControlMode, useArrows: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(move.name ?: "Move", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.weight(1f))
            move.tags?.let { tags ->
                if (tags.containsKey("hb")) PropertyIcon(Icons.Rounded.Whatshot, Color(0xFFD32F2F))
                if (tags.containsKey("he")) PropertyIcon(Icons.Rounded.Bolt, Color(0xFFD32F2F))
                if (tags.containsKey("pc")) PropertyIcon(Icons.Rounded.Shield, Color(0xFF2196F3))
                if (tags.containsKey("hom")) PropertyIcon(Icons.Rounded.Radar, Color(0xFFFFEB3B))
                if (tags.containsKey("trn")) PropertyIcon(Icons.Rounded.Tornado, Color(0xFF00BCD4))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) { VisualCommand(move.command ?: "", mode, useArrows) }
            Row {
                FrameBox("START", move.startup ?: "--")
                Spacer(Modifier.width(8.dp))
                val b = move.block ?: "--"
                val bCol = if (b.startsWith("+")) Color.Green else if (b.contains("-") && (b.filter { it.isDigit() }.toIntOrNull() ?: 0) >= 10) Color(0xFFD32F2F) else Color.White
                FrameBox("BLOCK", b, bCol)
            }
        }
    }
}

@Composable
fun PropertyIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp).padding(start = 6.dp))
}

@Composable
fun LegendItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Text(label, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp))
    }
}

@Composable
fun FrameBox(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(label, color = Color.Gray, fontSize = 8.sp)
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}