package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.SchemaSerializer
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface SkillTrigger {
    data object Jump : SkillTrigger
    data object Noop : SkillTrigger
}

object SkillTriggerSerializer : SchemaSerializer<SkillTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillTrigger {
        return SkillTrigger.Jump
    }
}