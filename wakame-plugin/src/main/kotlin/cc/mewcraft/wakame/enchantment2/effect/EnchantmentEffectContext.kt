package cc.mewcraft.wakame.enchantment2.effect

import cc.mewcraft.wakame.item.ItemSlot
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

data class EnchantmentEffectContext(
    val stack: ItemStack,
    val slot: ItemSlot,
    val owner: LivingEntity,
    val breakCallback: (Material) -> Unit,
)