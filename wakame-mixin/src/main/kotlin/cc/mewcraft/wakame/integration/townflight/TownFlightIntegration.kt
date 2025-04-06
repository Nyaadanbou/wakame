package cc.mewcraft.wakame.integration.townflight

import org.bukkit.Location
import org.bukkit.entity.Player

interface TownFlightIntegration {
    /**
     * Returns true if a player can fly according to TownyFlight's rules.
     *
     * @param player the {@link Player} to test for flight allowance
     * @param silent true will show messages to player
     * @return true if the {@link Player} is allowed to fly
     **/
    fun canFly(player: Player, silent: Boolean): Boolean

    /**
     * Returns true if a player is allowed to fly at their current location. Checks
     * if they are in the wilderness, in their own town and if not, whether they
     * have the allied towns permission and if they are in an allied area.
     *
     * @param player   the {@link Player}
     * @param location the {@link Location} to test for the player
     * @return true if player is allowed to be flying at their present location
     */
    fun allowedLocation(player: Player, location: Location): Boolean

    /**
     * Turn off flight from a {@link Player}.
     *
     * @param player the {@link Player} to take flight from
     * @param silent true will mean no message is shown to the {@link Player}
     * @param forced true if this is a forced deactivation or not
     * @param cause  String cause of disabling flight ("", "pvp", "console")
     */
    fun removeFlight(player: Player, silent: Boolean, forced: Boolean, cause: String)

    /**
     * Turn flight on for a {@link Player}.
     *
     * @param player the {@link Player} who receives flight
     * @param silent true will mean no message is shown to the {@link Player}
     */
    fun addFlight(player: Player, silent: Boolean)

    /**
     * Turn temporary flight on for a {@link Player}.
     *
     * @param player the {@link Player} who receives temporary flight
     * @param silent true will mean no message is shown to the {@link Player}
     * @param seconds the number of seconds to give flight for
     */
    fun addTempFlight(player: Player, seconds: Long, silent: Boolean)

    /**
     * Check if the {@link Player} is able to fly, and remove it if not able to.
     *
     * @param player the {@link Player} who is being tested
     * @param silent true will mean no message is shown to the {@link Player}
     */
    fun testForFlight(player: Player, silent: Boolean)

    /**
     * Get the number of seconds remaining for a {@link Player} to fly.
     *
     * @param player the {@link Player} to get the remaining flight time for
     */
    fun getFlightSecondsRemaining(player: Player): Long

    /**
     * Get the number of seconds remaining for a {@link Player} to fly in a pretty format.
     *
     * @param player the {@link Player} to get the remaining flight time for
     */
    fun getFlightSecondsRemainingPrettified(player: Player): String
}