@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryFreezeEvent
import org.bukkit.enchantments.Enchantment

/**
 * 表示如何注册自定义附魔的.
 */
interface CustomEnchantmentRegister {
    fun register(event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>)
}