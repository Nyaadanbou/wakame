package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.enchantment.wrapper.CriticalHit
import cc.mewcraft.wakame.enchantment.wrapper.ElementDamage
import cc.mewcraft.wakame.enchantment.wrapper.ElementProtection
import cc.mewcraft.wakame.registry.ElementRegistry
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.enchantments.Enchantment

/**
 * [CustomEnchantment] 的注册表.
 *
 * 该注册表在 post-world 之后才会初始化完成.
 * 在那之前, [get] 函数会永远返回 `null`.
 */
internal object CustomEnchantmentRegistry {
    fun all(): Set<CustomEnchantment> {
        return CustomEnchantmentInternals.all()
    }

    fun get(enchantment: Enchantment): CustomEnchantment? {
        return CustomEnchantmentInternals.get(enchantment)
    }

    fun initialize() {
        CustomEnchantmentInternals.initialize()
    }
}

private object CustomEnchantmentInternals {
    /**
     * 用于存储所有 [CustomEnchantment] 实例的映射.
     *
     * 这里之所以可以用 [Reference2ObjectMap] 是因为 Paper 的实现已经保证对于任何一个
     * [net.kyori.adventure.key.Key] 来说, 只会有一个 [Enchantment] 实例存在.
     */
    private val mapping: Reference2ObjectOpenHashMap<Enchantment, CustomEnchantment> = Reference2ObjectOpenHashMap()
    private val cached: Set<CustomEnchantment> by lazy { mapping.values.toHashSet() }

    /**
     * 获取所有的 [CustomEnchantment] 实例.
     */
    fun all(): Set<CustomEnchantment> {
        return cached
    }

    fun get(enchantment: Enchantment): CustomEnchantment? {
        return mapping[enchantment]
    }

    fun register(enchantment: Enchantment, customEnchantment: CustomEnchantment) {
        mapping[enchantment] = customEnchantment
    }

    fun initialize() {
        // 能用就行(

        val earth = ElementRegistry.INSTANCES["earth"]
        val fire = ElementRegistry.INSTANCES["fire"]
        val neutral = ElementRegistry.INSTANCES["neutral"]
        val thunder = ElementRegistry.INSTANCES["thunder"]
        val water = ElementRegistry.INSTANCES["water"]
        val wind = ElementRegistry.INSTANCES["wind"]

        register(Enchantments.CRITICAL_HIT, CriticalHit())
        registerElementDamage(Enchantments.EARTH_DAMAGE, earth)
        registerElementProtection(Enchantments.EARTH_PROTECTION, earth)
        registerElementDamage(Enchantments.FIRE_DAMAGE, fire)
        registerElementProtection(Enchantments.FIRE_PROTECTION, fire)
        registerElementDamage(Enchantments.NEUTRAL_DAMAGE, neutral)
        registerElementProtection(Enchantments.NEUTRAL_PROTECTION, neutral)
        registerElementDamage(Enchantments.THUNDER_DAMAGE, thunder)
        registerElementProtection(Enchantments.THUNDER_PROTECTION, thunder)
        registerElementDamage(Enchantments.WATER_DAMAGE, water)
        registerElementProtection(Enchantments.WATER_PROTECTION, water)
        registerElementDamage(Enchantments.WIND_DAMAGE, wind)
        registerElementProtection(Enchantments.WIND_PROTECTION, wind)
    }

    private fun registerElementDamage(enchantment: Enchantment, element: Element) {
        register(enchantment, ElementDamage(enchantment, element))
    }

    private fun registerElementProtection(enchantment: Enchantment, element: Element) {
        register(enchantment, ElementProtection(enchantment, element))
    }
}