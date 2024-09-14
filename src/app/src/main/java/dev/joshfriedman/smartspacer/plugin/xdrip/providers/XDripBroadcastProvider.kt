package dev.joshfriedman.smartspacer.plugin.xdrip.providers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerBroadcastProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import dev.joshfriedman.smartspacer.plugin.xdrip.complications.XDripComplication
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.DATASTORE_NAME
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgDisplayUnitsKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgSlopeKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgSlopeNameKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgTimestampKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.Intents
import kotlinx.coroutines.runBlocking

object BundleLogger {
    fun log(bundle: Bundle?): String {
        if (bundle == null) {
            return "null"
        }
        var string = "Bundle{"
        for (key in bundle.keySet()) {
            @Suppress("DEPRECATION")
            string += " " + key + " => " + bundle[key] + ";"
        }
        string += " }Bundle"
        return string
    }
}

class XDripBroadcastProvider : SmartspacerBroadcastProvider() {

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
    }

    override fun onReceive(intent: Intent) {
        val bundle = intent.extras ?: return
        Log.d("XDripBroadcast", "onReceive ${intent.action} ${BundleLogger.log(bundle)}")

        var bg = bundle.getDouble(Intents.EXTRA_BG_ESTIMATE)
        val bgSlope = bundle.getDouble(Intents.EXTRA_BG_SLOPE)
        val bgSlopeName = bundle.getString(Intents.EXTRA_BG_SLOPE_NAME)
        val bgDisplayUnits = bundle.getString(Intents.EXTRA_DISPLAY_UNITS)
        val bgTimestamp = bundle.getLong(Intents.EXTRA_TIMESTAMP)

        // Convert from mg/dL to mmol/L
        if (bgDisplayUnits == "mmol") {
            bg *= 0.0555
        }

        // TODO: Get image icon from slope name

        // Save result
        runBlocking {
            provideContext().dataStore.edit { settings ->
                settings[bgKey] = bg
                settings[bgSlopeKey] = bgSlope
                settings[bgSlopeNameKey] = bgSlopeName!!
                settings[bgDisplayUnitsKey] = bgDisplayUnits!!
                settings[bgTimestampKey] = bgTimestamp
            }
        }

        // Notify about change
        SmartspacerComplicationProvider.notifyChange(context!!, XDripComplication::class.java)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            intentFilters = listOf(IntentFilter("com.eveningoutpost.dexdrip.BgEstimate"))
        )
    }

}