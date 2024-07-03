package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

val Key.toNamespacedKey: NamespacedKey
    get() = NamespacedKey(this.namespace(), this.value())