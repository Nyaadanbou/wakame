package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.SchemaSerializer
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

// TODO 等技能框架确定后移除

interface SkillInstance {
    data object Noop : SkillInstance
}

interface SkillTrigger {
    data object Noop : SkillTrigger
}

object SkillInstanceSerializer : SchemaSerializer<SkillInstance> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillInstance {
        return SkillInstance.Noop
    }
}

object SkillTriggerSerializer : SchemaSerializer<SkillTrigger> {
    override fun deserialize(type: Type, node: ConfigurationNode): SkillTrigger {
        return SkillTrigger.Noop
    }
}