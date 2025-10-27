package cc.mewcraft.wakame.integration.playermana

import org.bukkit.entity.Player

/**
 * 代表一个玩家魔法值的容器.
 */
interface PlayerManaIntegration {

    val manaType: PlayerManaType

    /**
     * Gets the current mana of the player.
     *
     * @param player
     * @return the current mana
     */
    fun getMana(player: Player): Double

    /**
     * Sets the current mana of the player.
     *
     * @param player
     * @param amount the amount to set
     */
    fun setMana(player: Player, amount: Double)

    /**
     * Gets the maximum mana of the player.
     *
     * @param player
     * @return the maximum mana
     */
    fun getMaxMana(player: Player): Double

    /**
     * Attempts to consume the specified amount of mana, simulating using a mana ability. Will only consume if the user's mana is greater than or equal to amount. If the player does not have enough mana, a "Not enough mana" message will be sent to the user's action bar. Does not send any message if successful, you must handle that.
     *
     * @param player
     * @param amount the amount to consume
     * @return true if the user had enough mana and the operation was successful, false if not
     */
    fun consumeMana(player: Player, amount: Double): Boolean

    companion object : PlayerManaIntegration {

        override val manaType: PlayerManaType get() = currentImpl.manaType
        override fun getMana(player: Player): Double = currentImpl.getMana(player)
        override fun setMana(player: Player, amount: Double) = currentImpl.setMana(player, amount)
        override fun getMaxMana(player: Player): Double = currentImpl.getMaxMana(player)
        override fun consumeMana(player: Player, amount: Double): Boolean = currentImpl.consumeMana(player, amount)

        /**
         * 设置当前的实现.
         */
        fun setImplementation(impl: PlayerManaIntegration) {
            currentImpl = impl
        }

        /**
         * 无操作实现.
         */
        private val zeroImpl = object : PlayerManaIntegration {
            override val manaType: PlayerManaType = PlayerManaType.ZERO
            override fun getMana(player: Player): Double = 0.0
            override fun setMana(player: Player, amount: Double) {}
            override fun getMaxMana(player: Player): Double = 0.0
            override fun consumeMana(player: Player, amount: Double): Boolean = false
        }

        /**
         * 当前实现.
         */
        private var currentImpl: PlayerManaIntegration = zeroImpl
    }

}