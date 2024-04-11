package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import cc.mewcraft.wakame.skill.condition.SkillConditionFactory

object SkillConditionRegistry : Initializable {
    val INSTANCE: Registry<String, SkillConditionFactory<*, *>> = SimpleRegistry()

    override fun onPreWorld() {
        INSTANCE += "durability" to DurabilityCondition
    }
}