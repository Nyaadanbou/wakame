@file:JvmName("EnchantmentUtils")

package cc.mewcraft.wakame.enchantment2

import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.MINECRAFT_NAMESPACE
import cc.mewcraft.wakame.util.handle
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentType
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack


/**
 * 返回该魔咒的所有 *魔咒效果组件*.
 *
 * @see net.minecraft.world.item.enchantment.EnchantmentEffectComponents
 * @see cc.mewcraft.wakame.mixin.support.EnchantmentEffectComponentsPatch
 */
val Enchantment.effects: DataComponentMap
    get() = handle.effects()

fun <T> Enchantment.getEffects(type: DataComponentType<List<T>>): List<T> =
    handle.getEffects(type)

fun <T> Enchantment.getEffect(type: DataComponentType<T>): T? =
    handle.effects().get(type)

/**
 * 返回该物品上非 [MINECRAFT_NAMESPACE] 命名空间下的 [Enchantment].
 */
val ItemStack.customEnchantments: Map<Enchantment, Int>
    get() = enchantments.filterKeys { it.key.namespace() != MINECRAFT_NAMESPACE }

/**
 * 返回该物品上 [KOISH_NAMESPACE] 命名空间下的 [Enchantment].
 */
val ItemStack.koishEnchantments: Map<Enchantment, Int>
    get() = enchantments.filterKeys { it.key.namespace() == KOISH_NAMESPACE }
