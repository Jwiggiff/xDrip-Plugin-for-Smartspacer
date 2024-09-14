package dev.joshfriedman.smartspacer.plugin.xdrip.complications

import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.util.Log
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import dev.joshfriedman.smartspacer.plugin.xdrip.R
import dev.joshfriedman.smartspacer.plugin.xdrip.providers.XDripBroadcastProvider.Companion.dataStore
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DataStoreManager.Companion.bgSlopeNameKey
import dev.joshfriedman.smartspacer.plugin.xdrip.utility.DexConstants.TREND_ARROW_VALUES
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


class XDripComplication: SmartspacerComplicationProvider() {
    private val xdripPackageName = "com.eveningoutpost.dexdrip"

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val bg = runBlocking { provideContext().dataStore.data.first()[bgKey] }
        val bgSlopeName = runBlocking { provideContext().dataStore.data.first()[bgSlopeNameKey] }

        val bgIcon = TREND_ARROW_VALUES.getEnum(bgSlopeName).Symbol()


        return if (bg != null) listOf(
            ComplicationTemplate.Basic(
                id = "xdrip_$smartspacerId",
                icon = com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon(
                    Icon.createWithResource(
                        provideContext(),
                        R.drawable.xdrip
                    )
                ),
                content = Text("%.1f $bgIcon".format(bg)),
                onClick = TapAction(intent = provideContext().packageManager.getLaunchIntentForPackage(xdripPackageName))
            ).create()
        ) else emptyList()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "xDrip complication",
            description = "Shows your BG level from xDrip",
            icon = Icon.createWithResource(provideContext(), R.drawable.xdrip),
            broadcastProvider = "dev.joshfriedman.smartspacer.plugin.xdrip.broadcast.xdrip",
            compatibilityState = getCompatibilityState()
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        try {
            provideContext().packageManager.getPackageInfo(xdripPackageName, PackageManager.GET_ACTIVITIES)
        } catch (e: PackageManager.NameNotFoundException) {
            return CompatibilityState.Incompatible("xDrip is not installed!")
        }
        return CompatibilityState.Compatible
    }
}