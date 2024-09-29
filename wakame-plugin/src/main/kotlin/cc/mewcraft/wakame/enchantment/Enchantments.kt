@file:Suppress(
    "removal",
    "DEPRECATION",
    "OVERRIDE_DEPRECATION",
    "UnstableApiUsage",
)

package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.SharedConstants
import io.papermc.paper.enchantments.EnchantmentRarity
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.set.RegistryKeySet
import io.papermc.paper.registry.set.RegistrySet
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.EntityCategory
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

/**
 * 本单例包含了所有 *新增加的* / *修改过的* / *移除掉的* [Enchantment] 实例.
 *
 * 本单例存在的目的是为了方便获取由数据包移除,修改,新增的魔咒.
 * 本质上本单例只是省去了调用 [RegistryAccess] 的繁琐操作.
 */
/* public */ object Enchantments : KoinComponent {

    /* 新增加的魔咒 */
    val CRITICAL_HIT = getCustom("critical_hit")
    val EARTH_DAMAGE = getCustom("earth_damage")
    val EARTH_PROTECTION = getCustom("earth_protection")
    val FIRE_DAMAGE = getCustom("fire_damage")
    val FIRE_PROTECTION = getCustom("fire_protection")
    val NEUTRAL_DAMAGE = getCustom("neutral_damage")
    val NEUTRAL_PROTECTION = getCustom("neutral_protection")
    val THUNDER_DAMAGE = getCustom("thunder_damage")
    val THUNDER_PROTECTION = getCustom("thunder_protection")
    val WATER_DAMAGE = getCustom("water_damage")
    val WATER_PROTECTION = getCustom("water_protection")
    val WIND_DAMAGE = getCustom("wind_damage")
    val WIND_PROTECTION = getCustom("wind_protection")

    /* 修改过的魔咒 */
    val MINECRAFT_KNOCKBACK = getMinecraft("knockback")
    val MINECRAFT_PUNCH = getMinecraft("punch")

    /* 移除掉的魔咒 */
    val MINECRAFT_BANE_OF_ARTHROPODS = getMinecraft("bane_of_arthropods")
    val MINECRAFT_BLAST_PROTECTION = getMinecraft("blast_protection")
    val MINECRAFT_FEATHER_FALLING = getMinecraft("feather_falling")
    val MINECRAFT_FIRE_PROTECTION = getMinecraft("fire_protection")
    val MINECRAFT_IMPALING = getMinecraft("impaling")
    val MINECRAFT_INFINITY = getMinecraft("infinity")
    val MINECRAFT_MENDING = getMinecraft("mending")
    val MINECRAFT_MULTISHOT = getMinecraft("multishot")
    val MINECRAFT_PIERCING = getMinecraft("piercing")
    val MINECRAFT_POWER = getMinecraft("power")
    val MINECRAFT_PROJECTILE_PROTECTION = getMinecraft("projectile_protection")
    val MINECRAFT_SMITE = getMinecraft("smite")
    val MINECRAFT_THORNS = getMinecraft("thorns")

    /**
     * 获取指定的魔咒. 只有在服务器运行时才能成功返回.
     */
    private fun get(namespace: String, path: String): Enchantment {
        val key = Key.key(namespace, path)
        val registry = try {
            RegistryAccess.registryAccess().getRegistry<Enchantment>(RegistryKey.ENCHANTMENT)
        } catch (e: Exception) {
            get<Logger>().error("Can't get enchantment registry, returning empty for '$key'", e)
            return EmptyEnchantment
        }

        val enchantment = registry.get(key)
        if (enchantment == null) {
            get<Logger>().error("Can't find enchantment '$key', returning empty")
            return EmptyEnchantment
        }

        return enchantment
    }

    private fun getMinecraft(id: String): Enchantment {
        return get(Key.MINECRAFT_NAMESPACE, id)
    }

    private fun getCustom(id: String): Enchantment {
        return get(SharedConstants.PLUGIN_NAME, id)
    }
}

/**
 * 如果无法找到指定的魔咒，将会返回这个默认魔咒.
 */
private object EmptyEnchantment : Enchantment() {
    override fun getName(): String = "enchantment.wakame.error"
    override fun getMaxLevel(): Int = 1
    override fun getStartLevel(): Int = 1
    override fun getItemTarget(): EnchantmentTarget = EnchantmentTarget.ALL
    override fun isTreasure(): Boolean = false
    override fun isCursed(): Boolean = false
    override fun conflictsWith(other: Enchantment): Boolean = true
    override fun canEnchantItem(item: ItemStack): Boolean = false
    override fun displayName(level: Int): Component = text { content("ERROR"); color(NamedTextColor.RED) }
    override fun isTradeable(): Boolean = false
    override fun isDiscoverable(): Boolean = false
    override fun getMinModifiedCost(level: Int): Int = Int.MAX_VALUE
    override fun getMaxModifiedCost(level: Int): Int = Int.MAX_VALUE
    override fun getAnvilCost(): Int = Int.MAX_VALUE
    override fun getRarity(): EnchantmentRarity = EnchantmentRarity.COMMON
    override fun getDamageIncrease(level: Int, entityCategory: EntityCategory): Float = Float.NaN
    override fun getDamageIncrease(level: Int, entityType: EntityType): Float = Float.NaN
    override fun getActiveSlotGroups(): Set<EquipmentSlotGroup?> = emptySet()
    override fun description(): Component = text { content("ERROR"); color(NamedTextColor.RED) }
    override fun getSupportedItems(): RegistryKeySet<ItemType> = RegistrySet.keySetFromValues(RegistryKey.ITEM, listOf(ItemType.AIR))
    override fun getPrimaryItems(): RegistryKeySet<ItemType>? = null
    override fun getWeight(): Int = 1
    override fun getExclusiveWith(): RegistryKeySet<Enchantment> = RegistrySet.keySetFromValues(RegistryKey.ENCHANTMENT, emptyList())
    override fun translationKey(): String = "enchantment.wakame.error"
    override fun getKey(): NamespacedKey = NamespacedKey(SharedConstants.PLUGIN_NAME, "error")
    override fun getTranslationKey(): String = translationKey()
}