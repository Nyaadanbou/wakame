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
    val BLACK_HOLE = register("black_hole", BlackHoleArchetype)
    val BLINK = register("blink", BlinkArchetype)
    val DASH = register("dash", DashArchetype)
    val EXTRA_JUMP = register("extra_jump", ExtraJumpArchetype)

    private inline fun <reified T : AbilityArchetype> register(id: String, entry: T): T {
        return Registry.register(KoishRegistries.ABILITY_ARCHETYPE, id, entry)
    }
}