package cc.mewcraft.wakame.integration.playerlevel

enum class PlayerLevelType {
    /**
     * Level sourced from our own adventure level plugin.
     */
    ADVENTURE,

    /**
     * Level sourced from the Minecraft vanilla experience level.
     */
    VANILLA,

    /**
     * Level sourced from nothing, always returning 0 level.
     */
    ZERO,
}