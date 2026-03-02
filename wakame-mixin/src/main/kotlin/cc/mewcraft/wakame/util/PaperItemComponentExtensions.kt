package cc.mewcraft.wakame.util

import io.papermc.paper.datacomponent.item.DamageResistant
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.bukkit.damage.DamageSource

fun DamageResistant.isResistantTo(damageSource: DamageSource): Boolean {
    return RegistryAccess.registryAccess()
        .getRegistry(RegistryKey.DAMAGE_TYPE)
        .getTagOrNull(this.types())
        ?.contains(
            TypedKey.create(RegistryKey.DAMAGE_TYPE, damageSource.damageType.key)
        ) == true
}