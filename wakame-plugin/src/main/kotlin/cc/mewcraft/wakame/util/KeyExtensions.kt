package cc.mewcraft.wakame.util

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

val Key.asNamespacedKey: NamespacedKey
    get() = requireNotNull(NamespacedKey.fromString(this.asString())) { "Can't convert ${Key::class.qualifiedName} to {${NamespacedKey::class.qualifiedName}}" }