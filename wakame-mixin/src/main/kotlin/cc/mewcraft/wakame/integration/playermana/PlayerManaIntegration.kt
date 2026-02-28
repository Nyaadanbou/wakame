package cc.mewcraft.wakame.integration.playermana

import org.bukkit.entity.Player
import java.util.*

/**
 * 代表一个玩家魔法值的容器.
 */
interface PlayerManaIntegration {

    val type: PlayerManaType

    /**
     * Gets the current mana of the player.
     *
     * @param player
     * @return the current mana
     */
    fun getMana(player: Player): Double

    /**
     * Gets the current mana of the player by their UUID.
     *
     * @param playerId the UUID of the player
     * @return the current mana, or 0 if the player is not online or does not exist
     */
    fun getMana(playerId: UUID): Double

    /**
     * Sets the current mana of the player.
     *
     * @param player
     * @param amount the amount to set
     */
    fun setMana(player: Player, amount: Double)

    /**
     * Sets the current mana of the player by their UUID. Does nothing if the player is not online or does not exist.
     *
     * @param playerId the UUID of the player
     * @param amount the amount to set
     */
    fun setMana(playerId: UUID, amount: Double)

    /**
     * Gets the maximum mana of the player.
     *
     * @param player
     * @return the maximum mana
     */
    fun getMaxMana(player: Player): Double

    /**
     * Gets the maximum mana of the player by their UUID. Returns 0 if the player is not online or does not exist.
     *
     * @param playerId the UUID of the player
     * @return the maximum mana, or 0 if the player is not online or does not exist
     */
    fun getMaxMana(playerId: UUID): Double

    /**
     * Attempts to consume the specified amount of mana, simulating using a mana ability. Will only consume if the user's mana is greater than or equal to amount. If the player does not have enough mana, a "Not enough mana" message will be sent to the user's action bar. Does not send any message if successful, you must handle that.
     *
     * @param player
     * @param amount the amount to consume
     * @return true if the user had enough mana and the operation was successful, false if not
     */
    fun consumeMana(player: Player, amount: Double): Boolean

    /**
     * Attempts to consume the specified amount of mana by the player's UUID, simulating using a mana ability. Will only consume if the user's mana is greater than or equal to amount. If the player does not have enough mana, a "Not enough mana" message will be sent to the user's action bar. Does not send any message if successful, you must handle that. Returns false if the player is not online or does not exist.
     *
     * @param playerId the UUID of the player
     * @param amount the amount to consume
     * @return true if the user had enough mana and the operation was successful, false if not or if the player is not online or does not exist
     */
    fun consumeMana(playerId: UUID, amount: Double): Boolean

    companion object : PlayerManaIntegration {

        private var implementation: PlayerManaIntegration = object : PlayerManaIntegration {
            override val type: PlayerManaType = PlayerManaType.INFINITY
            override fun getMana(player: Player): Double = Double.MAX_VALUE
            override fun getMana(playerId: UUID): Double = Double.MAX_VALUE
            override fun setMana(player: Player, amount: Double) {}
            override fun setMana(playerId: UUID, amount: Double) {}
            override fun getMaxMana(player: Player): Double = Double.MAX_VALUE
            override fun getMaxMana(playerId: UUID): Double = Double.MAX_VALUE
            override fun consumeMana(player: Player, amount: Double): Boolean = true
            override fun consumeMana(playerId: UUID, amount: Double): Boolean = true
        }

        /**
         * 设置当前的实现.
         */
        fun setImplementation(impl: PlayerManaIntegration) {
            implementation = impl
        }

        override val type: PlayerManaType get() = implementation.type
        override fun getMana(player: Player): Double = implementation.getMana(player)
        override fun getMana(playerId: UUID): Double = implementation.getMana(playerId)
        override fun setMana(player: Player, amount: Double) = implementation.setMana(player, amount)
        override fun setMana(playerId: UUID, amount: Double) = implementation.setMana(playerId, amount)
        override fun getMaxMana(player: Player): Double = implementation.getMaxMana(player)
        override fun getMaxMana(playerId: UUID): Double = implementation.getMaxMana(playerId)
        override fun consumeMana(player: Player, amount: Double): Boolean = implementation.consumeMana(player, amount)
        override fun consumeMana(playerId: UUID, amount: Double): Boolean = implementation.consumeMana(playerId, amount)
    }
}