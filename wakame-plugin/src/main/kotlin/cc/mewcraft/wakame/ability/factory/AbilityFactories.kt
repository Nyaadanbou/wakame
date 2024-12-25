package cc.mewcraft.wakame.ability.factory

import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry
import cc.mewcraft.wakame.ability.factory.implement.Blink
import cc.mewcraft.wakame.ability.factory.implement.Dash

/**
 * 技能工厂的集合. 用于获取用于创建技能的工厂类.
 */
internal object AbilityFactories {
    private val FACTORIES: Registry<String, AbilityFactory<*>> = SimpleRegistry()

    fun load() {
        FACTORIES.register("blink", Blink)
        FACTORIES.register("dash", Dash)
    }

    operator fun get(registryName: String): AbilityFactory<*>? {
        return FACTORIES[registryName]
    }

}