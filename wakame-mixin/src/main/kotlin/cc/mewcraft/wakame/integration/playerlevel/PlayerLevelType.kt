package cc.mewcraft.wakame.integration.playerlevel

enum class PlayerLevelType {
    /**
     * Level sourced from the AuraSkills plugin.
     *
     * See [AuraSkills API](https://wiki.aurelium.dev/auraskills/api).
     */
    AURA_SKILLS,
    /**
     * Level sourced from our own adventure level plugin.
     *
     * See [AdventureLevel API](https://github.com/Nyaadanbou/adventurelevel).
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