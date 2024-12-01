package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import net.kyori.adventure.key.Key

interface SkillProvider {
    fun get(): Skill
}

fun SkillProvider(key: Key): SkillProvider {
    return KeyedSkillProvider(key)
}

private data class KeyedSkillProvider(
    override val key: Key,
) : SkillProvider, Keyed {
    override fun get(): Skill {
        return SkillRegistry.INSTANCES[key]
    }
}
