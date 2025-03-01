package cc.mewcraft.wakame.util

import me.lucko.shadow.*
import me.lucko.shadow.bukkit.BukkitShadowFactory

inline fun <reified T : Shadow> Any.shadow(): T {
    return BukkitShadowFactory.global().shadow<T>(this)
}

inline fun <reified T : Shadow> staticShadow(): T {
    return BukkitShadowFactory.global().staticShadow<T>()
}

inline fun <reified T : Shadow> constructShadow(vararg args: Any): T {
    return BukkitShadowFactory.global().constructShadow<T>(*args)
}

inline fun <reified T : Shadow> constructShadow(unwrapper: ShadowingStrategy.Unwrapper, vararg args: Any): T {
    return BukkitShadowFactory.global().constructShadow<T>(unwrapper, *args)
}
