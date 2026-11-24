package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.vaCarsDataStore: DataStore<Preferences> by preferencesDataStore(name = "va_cars_settings")

enum class ThemePreference(val storageValue: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromStorage(value: String?): ThemePreference {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}

data class SupabaseConfig(
    val url: String = "",
    val anonKey: String = "",
    val whatsappNumber: String = "",
    val isFromUserOverride: Boolean = false
) {
    val isConfigured: Boolean
        get() = isValidUrl(url) && isValidKey(anonKey)

    companion object {
        private const val PLACEHOLDER_URL = "YOUR_SUPABASE_URL"
        private const val PLACEHOLDER_KEY = "YOUR_SUPABASE_ANON_KEY"

        fun isValidUrl(raw: String): Boolean {
            val trimmed = raw.trim()
            return trimmed.isNotEmpty() &&
                !trimmed.contains(PLACEHOLDER_URL, ignoreCase = true) &&
                (trimmed.startsWith("https://", ignoreCase = true) ||
                    trimmed.startsWith("http://", ignoreCase = true))
        }

        fun isValidKey(raw: String): Boolean {
            val trimmed = raw.trim()
            return trimmed.isNotEmpty() &&
                !trimmed.contains(PLACEHOLDER_KEY, ignoreCase = true)
        }

        fun normalizeUrl(raw: String): String {
            val trimmed = raw.trim().trimEnd('/')
            if (trimmed.isEmpty()) return ""
            return if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
            ) {
                trimmed
            } else {
                "https://$trimmed"
            }
        }
    }
}

class SupabaseConfigStore(private val context: Context) {

    private object Keys {
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        val WHATSAPP_NUMBER = stringPreferencesKey("whatsapp_number")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }

    val configFlow: Flow<SupabaseConfig> = context.vaCarsDataStore.data.map { prefs ->
        val storedUrl = prefs[Keys.SUPABASE_URL].orEmpty().trim()
        val storedKey = prefs[Keys.SUPABASE_ANON_KEY].orEmpty().trim()
        val whatsapp = prefs[Keys.WHATSAPP_NUMBER].orEmpty().trim()

        val envUrl = BuildConfig.SUPABASE_URL.trim()
        val envKey = BuildConfig.SUPABASE_ANON_KEY.trim()

        val hasUserConfig = storedUrl.isNotEmpty() && storedKey.isNotEmpty()
        if (hasUserConfig) {
            SupabaseConfig(
                url = SupabaseConfig.normalizeUrl(storedUrl),
                anonKey = storedKey,
                whatsappNumber = whatsapp,
                isFromUserOverride = true
            )
        } else if (SupabaseConfig.isValidUrl(envUrl) && SupabaseConfig.isValidKey(envKey)) {
            SupabaseConfig(
                url = SupabaseConfig.normalizeUrl(envUrl),
                anonKey = envKey,
                whatsappNumber = whatsapp,
                isFromUserOverride = false
            )
        } else {
            SupabaseConfig(
                url = storedUrl,
                anonKey = storedKey,
                whatsappNumber = whatsapp,
                isFromUserOverride = false
            )
        }
    }

    val themePreferenceFlow: Flow<ThemePreference> = context.vaCarsDataStore.data.map { prefs ->
        ThemePreference.fromStorage(prefs[Keys.THEME_PREFERENCE])
    }

    suspend fun saveConfig(url: String, anonKey: String, whatsappNumber: String) {
        val normalizedUrl = SupabaseConfig.normalizeUrl(url)
        val cleanKey = anonKey.trim()
        val cleanWhatsapp = whatsappNumber.filter { it.isDigit() }
        context.vaCarsDataStore.edit { prefs ->
            prefs[Keys.SUPABASE_URL] = normalizedUrl
            prefs[Keys.SUPABASE_ANON_KEY] = cleanKey
            prefs[Keys.WHATSAPP_NUMBER] = cleanWhatsapp
        }
    }

    suspend fun clearSupabaseKeys() {
        context.vaCarsDataStore.edit { prefs ->
            prefs.remove(Keys.SUPABASE_URL)
            prefs.remove(Keys.SUPABASE_ANON_KEY)
        }
    }

    suspend fun saveThemePreference(theme: ThemePreference) {
        context.vaCarsDataStore.edit { prefs ->
            prefs[Keys.THEME_PREFERENCE] = theme.storageValue
        }
    }
}
