package com.example.tekkentournaments.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

object LanguageUtils {

    private const val PREFS_NAME = "app_settings"
    private const val KEY_LANG = "language_code"

    // 1. CAMBIAR Y GUARDAR IDIOMA
    fun setLocale(context: Context, languageCode: String) {
        // Guardamos la preferencia para siempre
        saveLanguagePreference(context, languageCode)

        // Aplicamos el cambio
        updateResources(context, languageCode)

        // Reiniciamos para ver los efectos
        if (context is Activity) {
            val intent = context.intent
            context.finish()
            context.startActivity(intent)
            context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // 2. CARGAR IDIOMA AL INICIAR LA APP
    fun loadLocale(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val language = prefs.getString(KEY_LANG, "") ?: ""

        if (language.isNotEmpty()) {
            updateResources(context, language)
        }
    }

    // 3. OBTENER EL IDIOMA ACTUAL (Para saber si poner "ES" o "EN" en el botón)
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Si no hay nada guardado, devolvemos el idioma del sistema o "es" por defecto
        return prefs.getString(KEY_LANG, Locale.getDefault().language) ?: "es"
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, languageCode).apply()
    }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}