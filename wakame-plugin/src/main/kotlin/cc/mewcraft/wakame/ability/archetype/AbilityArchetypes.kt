package cc.mewcraft.wakame.ability.archetype

import cc.mewcraft.wakame.ability.archetype.implement.BlackHoleArchetype
import cc.mewcraft.wakame.ability.archetype.implement.BlinkArchetype
import cc.mewcraft.wakame.ability.archetype.implement.DashArchetype
import cc.mewcraft.wakame.ability.archetype.implement.ExtraJumpArchetype
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry

/**
 * 技能工厂的集合. 用于获取用于创建技能的工厂类.
 */
internal object AbilityArchetypes {
    private val ARCHETYPES: Registry<String, AbilityArchetype> = SimpleRegistry()

    fun load() {
        ARCHETYPES.register("black_hole", BlackHoleArchetype)
        ARCHETYPES.register("blink", BlinkArchetype)
        ARCHETYPES.register("dash", DashArchetype)
        ARCHETYPES.register("extra_jump", ExtraJumpArchetype)
    }

    operator fun get(registryName: String): AbilityArchetype? {
        return ARCHETYPES[registryName]
    }

}