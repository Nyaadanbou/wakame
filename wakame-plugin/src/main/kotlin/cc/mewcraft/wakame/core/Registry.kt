package cc.mewcraft.wakame.core

import xyz.xenondevs.commons.provider.Provider

interface Registry<T> {
    val key: ResourceKey<out Registry<T>>
    fun get(id: ResourceLocation): Provider<T>
}

interface WritableRegistry<T> : Registry<T> {
    fun register(id: ResourceLocation, value: T)
}