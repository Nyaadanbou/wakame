@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment.register

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.enchantment.EnchantmentEffect
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup

interface CustomEnchantment : Keyed {
    /**
     * 用于注册附魔的 [TypedKey].
     */
    val enchantmentKey: TypedKey<Enchantment>

    /**
     * 该附魔的标签.
     */
    val tags: Collection<TagKey<Enchantment>>

    /**
     * 获取注册器.
     */
    fun getRegister(): CustomEnchantmentRegister

    /**
     * 获取附魔效果.
     */
    fun getEffects(level: Int): Collection<EnchantmentEffect>

    override val key: Key
        get() = enchantmentKey.key()

    val enchantment: Enchantment
        get() {
            return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getOrThrow(TypedKey.create(RegistryKey.ENCHANTMENT, key))
        }

    val equipmentSlotGroups: Set<EquipmentSlotGroup>
        get() = enchantment.activeSlotGroups
}