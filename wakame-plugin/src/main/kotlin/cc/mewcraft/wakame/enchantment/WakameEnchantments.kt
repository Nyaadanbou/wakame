@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.enchantment.impl.FireIncomingDamageRateProtection
import cc.mewcraft.wakame.enchantment.impl.UniversalIncomingDamageRateProtection
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.bukkit.enchantments.Enchantment

object WakameEnchantments {
    private val _ALL = ObjectArraySet<CustomEnchantment>()

    private fun createEnchantment(enchantment: CustomEnchantment): CustomEnchantment {
        _ALL.add(enchantment)
        return enchantment
    }

    val ALL: Set<CustomEnchantment>
        get() = _ALL

    val UNIVERSAL_INCOMING_DAMAGE_RATE: CustomEnchantment = createEnchantment(UniversalIncomingDamageRateProtection)
    val FIRE_INCOMING_DAMAGE_RATE: CustomEnchantment = createEnchantment(FireIncomingDamageRateProtection)

    /**
     * 获取自定义附魔.
     */
    fun get(key: TypedKey<Enchantment>): CustomEnchantment? {
        return ALL.find { it.enchantmentKey == key }
    }

    /**
     * 获取自定义附魔.
     */
    fun get(enchantment: Enchantment): CustomEnchantment? {
        val typedKey = TypedKey.create(RegistryKey.ENCHANTMENT, enchantment.key)
        return get(typedKey)
    }
}