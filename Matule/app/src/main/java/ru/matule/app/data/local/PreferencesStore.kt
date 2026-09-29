package ru.matule.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.IOException

/** Файл настроек приложения. Делегат должен быть один на процесс, поэтому объявлен на верхнем уровне. */
private val Context.matuleDataStore: DataStore<Preferences> by preferencesDataStore(name = "matule_prefs")

/**
 * Обёртка над DataStore Preferences: чтение и запись строк, флагов и JSON по ключу.
 * Все локальные источники данных работают через неё, а не с DataStore напрямую.
 *
 * @param context контекст приложения.
 */
class PreferencesStore(context: Context) {

    private val dataStore = context.applicationContext.matuleDataStore

    private val json = Json { ignoreUnknownKeys = true }

    /** Текущий снимок настроек; повреждённый файл читается как пустой. */
    private suspend fun snapshot(): Preferences = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .first()

    /** Строка по ключу или `null`. */
    suspend fun getString(key: String): String? = snapshot()[stringPreferencesKey(key)]

    /** Записывает строку; `null` удаляет значение. */
    suspend fun putString(key: String, value: String?) = putStrings(mapOf(key to value))

    /** Записывает несколько строк одной транзакцией; `null` удаляет значение. */
    suspend fun putStrings(values: Map<String, String?>) {
        dataStore.edit { prefs ->
            values.forEach { (key, value) ->
                val prefKey = stringPreferencesKey(key)
                if (value == null) prefs.remove(prefKey) else prefs[prefKey] = value
            }
        }
    }

    /** Флаг по ключу или `null`, если не задан. */
    suspend fun getBoolean(key: String): Boolean? = snapshot()[booleanPreferencesKey(key)]

    /** Записывает флаг. */
    suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { prefs -> prefs[booleanPreferencesKey(key)] = value }
    }

    /** Объект из JSON по ключу или `null`, если значения нет или оно не читается. */
    suspend fun <T> getJson(key: String, serializer: KSerializer<T>): T? =
        getString(key)?.let { raw -> runCatching { json.decodeFromString(serializer, raw) }.getOrNull() }

    /** Записывает объект как JSON; `null` удаляет значение. */
    suspend fun <T> putJson(key: String, serializer: KSerializer<T>, value: T?) =
        putString(key, value?.let { json.encodeToString(serializer, it) })

    /** Удаляет значения по ключам (ключи сравниваются по имени, тип значения не важен). */
    suspend fun remove(vararg keys: String) {
        dataStore.edit { prefs -> keys.forEach { prefs.remove(stringPreferencesKey(it)) } }
    }
}
