package cc.mewcraft.wakame.util

import io.papermc.paper.datacomponent.item.DamageResistant
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.damage.DamageSource

fun DamageResistant.isResistantTo(damageSource: DamageSource): Boolean {
    val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE)
    val tag = registry.getTagOrNull(this.types()) ?: return false
    // TODO TypedKey或许可以缓存以提高性能
    return tag.contains(TypedKey.create(RegistryKey.DAMAGE_TYPE, damageSource.damageType.key))
}