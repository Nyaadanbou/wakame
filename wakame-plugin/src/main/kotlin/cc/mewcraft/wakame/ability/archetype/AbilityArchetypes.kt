package cc.mewcraft.wakame.ability.archetype

import cc.mewcraft.wakame.ability.archetype.implement.BlackholeArchetype
import cc.mewcraft.wakame.ability.archetype.implement.BlinkArchetype
import cc.mewcraft.wakame.ability.archetype.implement.DashArchetype
import cc.mewcraft.wakame.ability.archetype.implement.MultiJumpArchetype
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.Registry

/**
 * 技能工厂的集合. 用于获取用于创建技能的工厂类.
 */
internal object AbilityArchetypes {
    val BLACKHOLE = register(BlackholeArchetype)
    val BLINK = register(BlinkArchetype)
    val DASH = register(DashArchetype)
    val MULTIJUMP = register(MultiJumpArchetype)

    private inline fun <reified T : AbilityArchetype> register(entry: T): T {
        return Registry.register(KoishRegistries.ABILITY_ARCHETYPE, entry.key, entry)
    }
}