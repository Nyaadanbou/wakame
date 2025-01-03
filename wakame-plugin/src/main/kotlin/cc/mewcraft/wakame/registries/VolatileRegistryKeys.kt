package cc.mewcraft.wakame.registries

import cc.mewcraft.wakame.core.ResourceKey
import cc.mewcraft.wakame.core.ResourceLocation
import cc.mewcraft.wakame.item.NekoItem

// 属于曲线救国范畴内的注册表
object VolatileRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = ResourceLocation.defaultNamespace("experimental")

    ///

    @JvmField
    val VANILLA_PROXY_ITEM = createRegistryKey<NekoItem>("vanilla_proxy_item")

    @Suppress("SameParameterValue")
    private fun <T> createRegistryKey(name: String): ResourceKey<T> {
        return ResourceKey.create<T>(ROOT_REGISTRY_NAME, ResourceLocation.defaultNamespace(name))
    }
}