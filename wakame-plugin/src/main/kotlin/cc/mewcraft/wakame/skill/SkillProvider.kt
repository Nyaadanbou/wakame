package cc.mewcraft.wakame.skill

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.registry.SkillRegistry
import net.kyori.adventure.key.Key

data class SkillProvider(
    val key: Key
) : Provider<Skill>() {
    override fun loadValue(): Skill {
        return SkillRegistry.INSTANCES[key]
    }
}
