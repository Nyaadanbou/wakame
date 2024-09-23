@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment.impl

import cc.mewcraft.wakame.enchantment.CustomEnchantment
import cc.mewcraft.wakame.enchantment.CustomEnchantmentRegister
import cc.mewcraft.wakame.enchantment.WakameEnchantmentsSupport
import cc.mewcraft.wakame.enchantment.WakameEnchantmentsSupport.applyCommonProperties
import cc.mewcraft.wakame.enchantment.WakameEnchantmentsSupport.protectionProperties
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryFreezeEvent
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment

data object UniversalIncomingDamageRateProtection : CustomEnchantment {
    override val enchantmentKey: TypedKey<Enchantment> = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("wakame", "universal_incoming_damage_rate"))
    override val tags: Collection<TagKey<Enchantment>> = listOf(
        *WakameEnchantmentsSupport.ENCHANTMENT_EFFECT_TAGS,
        EnchantmentTagKeys.EXCLUSIVE_SET_ARMOR
    )

    override fun getRegister(): CustomEnchantmentRegister {
        return UniversalIncomingDamageRateProtectionRegister(this)
    }
}

private class UniversalIncomingDamageRateProtectionRegister(
    private val enchantment: UniversalIncomingDamageRateProtection,
) : CustomEnchantmentRegister {
    override fun register(event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>) {
        val enchantmentKey = enchantment.enchantmentKey
        event.registry().register(enchantmentKey) { builder ->
            builder.applyCommonProperties(enchantmentKey, 4)
                .protectionProperties(event)
                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
                .anvilCost(1)
                .weight(10)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
        }
    }
}