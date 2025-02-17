package cc.mewcraft.wakame.enchantment2

import cc.mewcraft.wakame.util.handle
import net.kyori.adventure.key.Key
import net.minecraft.core.component.DataComponentType
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

/**
 * @see net.minecraft.world.item.enchantment.EnchantmentEffectComponents
 * @see cc.mewcraft.wakame.mixin.support.EnchantmentEffectComponentsPatch
 */
fun <T> Enchantment.getEffects(type: DataComponentType<List<T>>): List<T> {
    return handle.getEffects(type)
}

/**
 * 返回该物品上所有的非原版附魔.
 */
val ItemStack.koishEnchantments: Map<Enchantment, Int>
    get() = enchantments.filterKeys { it.key.namespace() != Key.MINECRAFT_NAMESPACE }
