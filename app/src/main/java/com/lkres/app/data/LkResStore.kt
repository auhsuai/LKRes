package com.lkres.app.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lkres.app.core.BandColor
import com.lkres.app.ui.bands.BandsMode
import com.lkres.app.ui.bands.BandsState
import com.lkres.app.ui.resistor.ResistorView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.lkresDataStore: DataStore<Preferences> by preferencesDataStore(name = "lkres_prefs")

object LkResStore {

    private const val TAG = "LkResStore"
    private const val EMPTY_SLOT = "-"

    private val KEY_MODE = stringPreferencesKey("mode")
    private val KEY_BAND_COUNT = intPreferencesKey("band_count")
    private val KEY_SELECTED = stringPreferencesKey("selected")
    private val KEY_ACTIVE_BAND = intPreferencesKey("active_band")
    private val KEY_PARALLEL_RESULTS = booleanPreferencesKey("parallel_results")
    private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    private val KEY_RESISTOR_VIEW = stringPreferencesKey("resistor_view")

    val bands = BandsState()

    private var _parallelResults by mutableStateOf(true)
    private var _keepScreenOn by mutableStateOf(false)
    private var _resistorView by mutableStateOf(ResistorView.BANDS)

    val parallelResults: Boolean
        get() = _parallelResults
    val keepScreenOn: Boolean
        get() = _keepScreenOn
    val resistorView: ResistorView
        get() = _resistorView

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var appContext: Context? = null

    @Volatile
    private var loaded = false

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        ioScope.launch { load() }
    }

    private suspend fun load() {
        val ctx = appContext ?: return
        try {
            val prefs = ctx.lkresDataStore.data.catch { e ->
                if (e is IOException) {
                    Log.w(TAG, "load: file prefs đọc không được, dùng giá trị mặc định", e)
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }.first()
            restore(prefs)
        } catch (e: Exception) {
            Log.w(TAG, "load thất bại, giữ state mặc định", e)
        } finally {
            loaded = true
        }
    }

    internal fun restore(prefs: Preferences) {
        bands.setMode(
            runCatching { BandsMode.valueOf(prefs[KEY_MODE] ?: BandsMode.AUTO.name) }
                .getOrDefault(BandsMode.AUTO)
        )
        parseSelected(prefs[KEY_SELECTED])?.let { colors ->
            bands.applyColors(colors)
            bands.setActiveBand(prefs[KEY_ACTIVE_BAND] ?: 0)
        }
        _parallelResults = prefs[KEY_PARALLEL_RESULTS] ?: true
        _keepScreenOn = prefs[KEY_KEEP_SCREEN_ON] ?: false
        _resistorView = runCatching {
            ResistorView.valueOf(prefs[KEY_RESISTOR_VIEW] ?: ResistorView.BANDS.name)
        }.getOrDefault(ResistorView.BANDS)
    }

    fun persistBands() {
        if (!loaded) return
        val ctx = appContext ?: return
        val mode = bands.mode
        val count = bands.bandCount
        val selected = bands.selected
        val activeBand = bands.activeBand
        ioScope.launch {
            try {
                ctx.lkresDataStore.edit { p ->
                    p[KEY_MODE] = mode.name
                    p[KEY_BAND_COUNT] = count
                    p[KEY_SELECTED] = encodeSelected(selected)
                    p[KEY_ACTIVE_BAND] = activeBand
                }
            } catch (e: Exception) {
                Log.w(TAG, "persistBands thất bại (mode=$mode, bandCount=$count)", e)
            }
        }
    }

    fun setParallelResults(value: Boolean) {
        _parallelResults = value
        savePref(KEY_PARALLEL_RESULTS, value)
    }

    fun setKeepScreenOn(value: Boolean) {
        _keepScreenOn = value
        savePref(KEY_KEEP_SCREEN_ON, value)
    }

    fun setResistorView(value: ResistorView) {
        _resistorView = value
        savePref(KEY_RESISTOR_VIEW, value.name)
    }

    internal fun encodeSelected(colors: List<BandColor?>): String =
        colors.joinToString(",") { it?.name ?: EMPTY_SLOT }

    internal fun parseSelected(raw: String?): List<BandColor?>? {
        if (raw.isNullOrEmpty()) return null
        return raw.split(",")
            .map { token ->
                if (token == EMPTY_SLOT) null else runCatching { BandColor.valueOf(token) }.getOrNull()
            }
            .takeIf { it.size in BandsState.MIN_BAND_COUNT..BandsState.MAX_BAND_COUNT }
    }

    private fun <T> savePref(key: Preferences.Key<T>, value: T) {
        val ctx = appContext ?: return
        ioScope.launch {
            try {
                ctx.lkresDataStore.edit { it[key] = value }
            } catch (e: Exception) {
                Log.w(TAG, "savePref ${key.name} thất bại", e)
            }
        }
    }
}
