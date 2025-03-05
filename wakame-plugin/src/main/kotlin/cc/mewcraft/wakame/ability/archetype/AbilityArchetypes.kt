package cc.mewcraft.wakame.ability.archetype

import cc.mewcraft.wakame.ability.archetype.implement.BlackHoleArchetype
import cc.mewcraft.wakame.ability.archetype.implement.BlinkArchetype
import cc.mewcraft.wakame.ability.archetype.implement.DashArchetype
import cc.mewcraft.wakame.ability.archetype.implement.ExtraJumpArchetype
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.Registry

/**
 * 技能工厂的集合. 用于获取用于创建技能的工厂类.
 */
internal object AbilityArchetypes {
    val BLACK_HOLE = register(BlackHoleArchetype)
    val BLINK = register(BlinkArchetype)
    val DASH = register(DashArchetype)
    val EXTRA_JUMP = register(ExtraJumpArchetype)

    private inline fun <reified T : AbilityArchetype> register(entry: T): T {
        return Registry.register(KoishRegistries.ABILITY_ARCHETYPE, entry.key, entry)
    }
}