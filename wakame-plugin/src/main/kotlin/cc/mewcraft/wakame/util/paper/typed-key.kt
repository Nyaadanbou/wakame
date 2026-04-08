@file:JvmName("TypedKeyExtra")

package cc.mewcraft.wakame.util.paper

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.TypedKey
import org.bukkit.Keyed

fun <T : Keyed> TypedKey<T>.getValue(): T? {
    return RegistryAccess.registryAccess().getRegistry(registryKey()).get(key())
}

fun <T : Keyed> TypedKey<T>.getValueOrThrow(): T {
    return RegistryAccess.registryAccess().getRegistry(registryKey()).getOrThrow(key())
}
