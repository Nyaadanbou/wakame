package cc.mewcraft.wakame.skill.trigger

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

data class SkillWithTrigger(
    val key: Key,
    val trigger: Trigger
)

fun SkillWithTrigger(core: BinarySkillCore): SkillWithTrigger {
    return SkillWithTrigger(core.key, core.trigger)
}

internal object SkillWithTriggerSerializer : SchemaSerializer<SkillWithTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillWithTrigger {
        val key = node.node("key").krequire<Key>()
        val trigger = node.node("trigger").krequire<Trigger>()
        return SkillWithTrigger(key, trigger)
    }
}