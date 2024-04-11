package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skill.type.RemovePotionEffect
import cc.mewcraft.wakame.skill.type.SkillFactory


object SkillTypeRegistry : Initializable {
    val INSTANCE: Registry<String, SkillFactory<*>> = SimpleRegistry()

    override fun onPreWorld() {
        INSTANCE += "remove_potion_effect" to RemovePotionEffect
    }
}