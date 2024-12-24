package cc.mewcraft.wakame.skill2.factory

import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry
import cc.mewcraft.wakame.skill2.factory.implement.Blink
import cc.mewcraft.wakame.skill2.factory.implement.Dash

/**
 * 技能工厂的集合. 用于获取用于创建技能的工厂类.
 */
internal object SkillFactories {
    private val FACTORIES: Registry<String, SkillFactory<*>> = SimpleRegistry()

    fun load() {
        FACTORIES.register("blink", Blink)
        FACTORIES.register("dash", Dash)
    }

    operator fun get(registryName: String): SkillFactory<*>? {
        return FACTORIES[registryName]
    }

}