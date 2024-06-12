package cc.mewcraft.wakame.skill.trigger

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type

data class ConfiguredSkill(
    val key: Key,
    val trigger: Trigger,
    /**
     * 此技能的有效变种，-1表示任意变种
     */
    val effectiveVariant: Int
)

fun ConfiguredSkill(core: BinarySkillCore): ConfiguredSkill {
    return ConfiguredSkill(core.key, core.trigger, core.effectiveVariant)
}

internal object ConfiguredSkillSerializer : SchemaSerializer<ConfiguredSkill> {
    override fun deserialize(type: Type, node: ConfigurationNode): ConfiguredSkill {
        val key = node.node("key").krequire<Key>()
        val trigger = node.node("trigger").krequire<Trigger>()
        val effectiveVariant = node.node("effectiveVariant").get<Int>() ?: -1
        return ConfiguredSkill(key, trigger, effectiveVariant)
    }
}