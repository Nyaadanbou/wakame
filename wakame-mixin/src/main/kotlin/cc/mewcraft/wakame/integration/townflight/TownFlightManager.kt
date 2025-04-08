package cc.mewcraft.wakame.integration.townflight

import org.bukkit.Location
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

object TownFlightManager {

    @ApiStatus.Internal
    var integration: TownFlightIntegration? = null

    fun canFly(player: Player, silent: Boolean): Result<Boolean> {
        return execute { this.canFly(player, silent) }
    }

    fun allowedLocation(player: Player, location: Location): Result<Boolean> {
        return execute { this.allowedLocation(player, location) }
    }

    fun removeFlight(player: Player, silent: Boolean, forced: Boolean, cause: String): Result<Unit> {
        return execute { this.removeFlight(player, silent, forced, cause) }
    }

    fun addFlight(player: Player, silent: Boolean): Result<Unit> {
        return execute { this.addFlight(player, silent) }
    }

    fun addTempFlight(player: Player, silent: Boolean, seconds: Long): Result<Unit> {
        return execute { this.addTempFlight(player, seconds, silent) }
    }

    fun testForFlight(player: Player, silent: Boolean): Result<Unit> {
        return execute { this.testForFlight(player, silent) }
    }

    private inline fun <T> execute(block: TownFlightIntegration.() -> T): Result<T> {
        val integration = this.integration
        if (integration == null) {
            return Result.failure(TownyNotAvailable())
        } else {
            return runCatching { block(integration) }
        }
    }
}

class TownyNotAvailable(message: String? = null, cause: Throwable? = null) : Throwable(message, cause)
