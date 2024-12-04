package cc.mewcraft.wakame.hook.impl.townyflight

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.townflight.TownFlightIntegration
import com.gmail.llmdlio.townyflight.TownyFlightAPI
import com.gmail.llmdlio.townyflight.config.Settings
import com.gmail.llmdlio.townyflight.tasks.TempFlightTask
import com.gmail.llmdlio.townyflight.util.Message
import com.gmail.llmdlio.townyflight.util.MetaData
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.util.TimeMgmt
import org.bukkit.Location
import org.bukkit.entity.Player

@Hook(plugins = ["TownyFlight"])
object TownyFlightHook : TownFlightIntegration {

    override fun canFly(player: Player, silent: Boolean): Boolean {
        return TownyFlightAPI.getInstance().canFly(player, silent)
    }

    override fun allowedLocation(player: Player, location: Location): Boolean {
        val resident = TownyAPI.getInstance().getResident(player) ?: return false
        return TownyFlightAPI.allowedLocation(player, location, resident)
    }

    override fun removeFlight(player: Player, silent: Boolean, forced: Boolean, cause: String) {
        return TownyFlightAPI.getInstance().removeFlight(player, silent, forced, cause)
    }

    override fun addFlight(player: Player, silent: Boolean) {
        return TownyFlightAPI.getInstance().addFlight(player, silent)
    }

    override fun addTempFlight(player: Player, seconds: Long, silent: Boolean) {
        val uuid = player.uniqueId
        val formattedTimeValue = TimeMgmt.getFormattedTimeValue(seconds * 1000.0)
        MetaData.addTempFlight(uuid, seconds.toLong())

        if (player.isOnline) {
            TempFlightTask.addPlayerTempFlightSeconds(uuid, seconds.toLong())
            Message.of(String.format(Message.getLangString("youHaveReceivedTempFlight"), formattedTimeValue)).to(player)

            if (Settings.autoEnableFlight && TownyFlightAPI.getInstance().canFly(player, true)) {
                TownyFlightAPI.getInstance().addFlight(player, Settings.autoEnableSilent) //
            }
        }
    }

    override fun testForFlight(player: Player, silent: Boolean) {
        return TownyFlightAPI.getInstance().testForFlight(player, silent)
    }

    override fun getFlightSecondsRemaining(player: Player): Long {
        return TempFlightTask.getSeconds(player.uniqueId)
    }

    override fun getFlightSecondsRemainingPrettified(player: Player): String {
        return TimeMgmt.getFormattedTimeValue(getFlightSecondsRemaining(player) * 1000.0)
    }
}