@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryFreezeEvent
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup

object WakameEnchantmentsSupport {
    /**
     * 附魔效果标签.
     *
     * 当这些标签从附魔上移除时，附魔将起到被禁用的效果 (即无法通过生存模式获取).
     */
    val ENCHANTMENT_EFFECT_TAGS = arrayOf(
        EnchantmentTagKeys.IN_ENCHANTING_TABLE,
        EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT,
        EnchantmentTagKeys.ON_RANDOM_LOOT,
        EnchantmentTagKeys.ON_TRADED_EQUIPMENT,
        EnchantmentTagKeys.TRADEABLE
    )

    /**
     * 允许的原版附魔效果.
     */
    val ALLOWED_ENCHANTMENTS = arrayOf(
        EnchantmentKeys.RESPIRATION,       // 水下呼吸
        EnchantmentKeys.DEPTH_STRIDER,     // 深海探索者
        EnchantmentKeys.FROST_WALKER,      // 冰霜行者
        EnchantmentKeys.BINDING_CURSE,     // 绑定诅咒
        EnchantmentKeys.SOUL_SPEED,        // 灵魂疾行
        EnchantmentKeys.SWIFT_SNEAK,       // 迅捷潜行
        EnchantmentKeys.FIRE_ASPECT,       // 火焰附加
        EnchantmentKeys.LOOTING,           // 抢夺
        EnchantmentKeys.EFFICIENCY,        // 效率
        EnchantmentKeys.SILK_TOUCH,        // 精准采集
        EnchantmentKeys.UNBREAKING,        // 耐久
        EnchantmentKeys.FORTUNE,           // 时运
        EnchantmentKeys.FLAME,             // 火矢
        EnchantmentKeys.LUCK_OF_THE_SEA,   // 海之眷顾
        EnchantmentKeys.LURE,              // 饵钓
        EnchantmentKeys.LOYALTY,           // 忠诚
        EnchantmentKeys.RIPTIDE,           // 激流
        EnchantmentKeys.CHANNELING,        // 引雷
        EnchantmentKeys.QUICK_CHARGE,      // 快速装填
        EnchantmentKeys.VANISHING_CURSE    // 消失诅咒
    )

    fun EnchantmentRegistryEntry.Builder.applyCommonProperties(
        enchantmentKey: TypedKey<Enchantment>,
        maxLevel: Int = 1,
    ): EnchantmentRegistryEntry.Builder {
        description(Component.translatable(getTranslateKey(enchantmentKey)))
        maxLevel(maxLevel)
        return this
    }

    fun EnchantmentRegistryEntry.Builder.protectionProperties(
        event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ): EnchantmentRegistryEntry.Builder {
        activeSlots(EquipmentSlotGroup.ARMOR)
        exclusiveWith(event.getOrCreateTag(EnchantmentTagKeys.EXCLUSIVE_SET_ARMOR))
        return this
    }


    fun getTranslateKey(key: TypedKey<*>): String {
        val enchantmentKey = key.key()
        return "enchantment.${enchantmentKey.namespace()}.${enchantmentKey.value()}"
    }
}