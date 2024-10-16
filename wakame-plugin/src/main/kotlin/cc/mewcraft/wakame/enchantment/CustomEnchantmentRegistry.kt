package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.enchantment.CustomEnchantmentRegistry.get
import cc.mewcraft.wakame.enchantment.wrapper.implementation.*
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.enchantments.Enchantment
import kotlin.collections.set

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

    /**
     * 获取所有的 [CustomEnchantment] 实例.
     */
    fun all(): Set<CustomEnchantment> {
        return mapping.values.toHashSet()
    }

    fun get(enchantment: Enchantment): CustomEnchantment? {
        return mapping[enchantment]
    }

    fun register(customEnchantment: CustomEnchantment) {
        mapping[customEnchantment.handle] = customEnchantment
    }

    fun initialize() {
        // 有点 hardcode, 能用就行(

        register(Agility())
        register(CriticalHit())
        register(DeepSearch())
        register(Disarray())
        register(ElementDamage("earth"))
        register(ElementProtection("earth"))
        register(ElementDamage("fire"))
        register(ElementProtection("fire"))
        register(ElementDamage("neutral"))
        register(ElementProtection("neutral"))
        register(ElementDamage("thunder"))
        register(ElementProtection("thunder"))
        register(ElementDamage("water"))
        register(ElementProtection("water"))
        register(ElementDamage("wind"))
        register(ElementProtection("wind"))
        register(Vitality())
    }
}