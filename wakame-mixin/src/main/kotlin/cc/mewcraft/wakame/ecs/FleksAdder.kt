package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.registry2.BuiltInRegistries

/**
 * 用于向 Fleks 中添加 [com.github.quillraven.fleks.Family] 与 [com.github.quillraven.fleks.IntervalSystem] 的方便接口.
 */
interface FleksAdder {
    fun addToRegistrySystem(id: String, bootstrapper: SystemBootstrapper) {
        BuiltInRegistries.SYSTEM_BOOTSTRAPPER.add(id, bootstrapper)
    }

    fun addToRegistryFamily(id: String, bootstrapper: FamiliesBootstrapper) {
        BuiltInRegistries.FAMILIES_BOOTSTRAPPER.add(id, bootstrapper)
    }
}