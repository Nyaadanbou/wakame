package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

data class ConfiguredSkillWithTrigger(
    val key: Key,
    val trigger: SkillTrigger
)

val ConfiguredSkillWithTrigger.skill
    get() = SkillRegistry.INSTANCE[key]

internal object ConfiguredSkillWithTriggerSerializer : SchemaSerializer<ConfiguredSkillWithTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): ConfiguredSkillWithTrigger {
        val key = node.node("key").krequire<Key>()
        val trigger = node.node("trigger").krequire<SkillTrigger>()
        return ConfiguredSkillWithTrigger(key, trigger)
    }
}