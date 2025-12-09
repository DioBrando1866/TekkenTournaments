import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth // Importa la clase Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

val supabase = createSupabaseClient(
    supabaseUrl = "https://nfnordyuqwenbgjzvlql.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5mbm9yZHl1cXdlbmJnanp2bHFsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIxODM3NzAsImV4cCI6MjA3Nzc1OTc3MH0.O1waEdKdO0a3ecGVBIhc5c1b-1yul9tqJr1nUYJZeSc"
) {

    defaultSerializer = KotlinXSerializer(Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    })

    install(Postgrest)
    install(Auth)
    install(Storage)
}