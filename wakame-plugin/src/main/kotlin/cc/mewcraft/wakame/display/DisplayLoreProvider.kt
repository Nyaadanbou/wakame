package cc.mewcraft.wakame.display

/**
 * Represents something that can provide a [LoreLine] for the object.
 *
 * The [LoreLine] is essentially a description of the `display lore`.
 */
interface DisplayLoreProvider {
    /**
     * Provides a [LoreLine] that describes the `display lore`.
     */
    fun provideDisplayLore(): LoreLine
}