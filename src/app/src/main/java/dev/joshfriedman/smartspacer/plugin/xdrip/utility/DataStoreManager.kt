package dev.joshfriedman.smartspacer.plugin.xdrip.utility

import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

class DataStoreManager {
    companion object {
        const val DATASTORE_NAME = "xdrip_settings"

        val bgKey = doublePreferencesKey("bg")
        val bgSlopeKey = doublePreferencesKey("bg_slope")
        val bgSlopeNameKey = stringPreferencesKey("bg_slope_name")
        val bgDisplayUnitsKey = stringPreferencesKey("bg_display_units")
        val bgTimestampKey = longPreferencesKey("bg_timestamp")
    }
}