package cc.mewcraft.wakame.enchantment

import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.collections.mapKeysNotNull

/**
 * @see CustomEnchantmentRegistry
 */
internal val Enchantment.custom: CustomEnchantment?
    get() = CustomEnchantmentRegistry.get(this)

/**
 * @see CustomEnchantmentRegistry
 */
internal val ItemStack.customEnchantments: Map<CustomEnchantment, Int>
    get() = enchantments.mapKeysNotNull { (enchantment, _) -> enchantment.custom }