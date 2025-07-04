package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.registry2.BuiltInRegistries

/**
 * 用于在 Fleks 中添加系统和家庭的方便接口.
 */
interface FleksAdder {
    fun addToRegistrySystem(id: String, bootstrapper: SystemBootstrapper) {
        BuiltInRegistries.SYSTEM_BOOTSTRAPPER.add(id, bootstrapper)
    }

    fun addToRegistryFamily(id: String, bootstrapper: FamiliesBootstrapper) {
        BuiltInRegistries.FAMILIES_BOOTSTRAPPER.add(id, bootstrapper)
    }
}