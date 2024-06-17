package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry
import cc.mewcraft.wakame.skill.factory.CommandExecute
import cc.mewcraft.wakame.skill.factory.Dash
import cc.mewcraft.wakame.skill.factory.KillEntity
import cc.mewcraft.wakame.skill.factory.RemovePotionEffect
import cc.mewcraft.wakame.skill.factory.SkillFactory
import cc.mewcraft.wakame.skill.factory.Teleport

/**
 * 技能工厂的集合. 用于获取用于创建技能的工厂类.
 */
internal object SkillFactories {
    private val FACTORIES: Registry<String, SkillFactory<*>> = SimpleRegistry()

    fun load() {
        FACTORIES.register("command_execute", CommandExecute)
        FACTORIES.register("dash", Dash)
        FACTORIES.register("kill_entity", KillEntity)
        FACTORIES.register("remove_potion_effect", RemovePotionEffect)
        FACTORIES.register("teleport", Teleport)
    }

    operator fun get(registryName: String): SkillFactory<*>? {
        return FACTORIES.find(registryName)
    }

}