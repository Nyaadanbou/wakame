package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.level.CUSTOM_ADVENTURE_LEVEL
import cc.mewcraft.wakame.level.PlayerLevelGetter
import cc.mewcraft.wakame.level.PlayerLevelType
import cc.mewcraft.wakame.level.VANILLA_EXPERIENCE_LEVEL
import com.google.common.collect.Range
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

data class PlayerLevelFilter(
    override val invert: Boolean,
    private val subtype: PlayerLevelType,
    private val level: Range<Int>,
) : KoinComponent, Filter {

    private val adventureLevelGetter: PlayerLevelGetter by inject<PlayerLevelGetter>(named(CUSTOM_ADVENTURE_LEVEL))
    private val experienceLevelGetter: PlayerLevelGetter by inject<PlayerLevelGetter>(named(VANILLA_EXPERIENCE_LEVEL))

    /**
     * Returns `true` if the player level in the [context] is in the range of
     * [level].
     */
    override fun test0(context: SchemeGenerationContext): Boolean {
        val playerLevel = when (subtype) {
            PlayerLevelType.ADVENTURE_LEVEL -> context.playerObject?.let(adventureLevelGetter::get)
            PlayerLevelType.EXPERIENCE_LEVEL -> context.playerObject?.let(experienceLevelGetter::get)
        } ?: 1 // returns level 1 if we can't get the expected level

        return playerLevel in level
    }
}