package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.registry.SkillRegistry
import net.kyori.adventure.key.Key
import java.util.function.Supplier

data class SkillProvider(
    val key: Key,
) : Supplier<Skill> {
    override fun get(): Skill {
        return SkillRegistry.INSTANCES[key]
    }
}
