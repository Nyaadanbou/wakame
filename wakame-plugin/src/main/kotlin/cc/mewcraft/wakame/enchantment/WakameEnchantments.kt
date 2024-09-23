@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.enchantment

import cc.mewcraft.wakame.enchantment.WakameEnchantmentsSupport.applyCommonProperties
import cc.mewcraft.wakame.enchantment.WakameEnchantmentsSupport.getTranslateKey
import cc.mewcraft.wakame.enchantment.WakameEnchantmentsSupport.protectionProperties
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryEvents
import io.papermc.paper.registry.event.RegistryFreezeEvent
import io.papermc.paper.registry.keys.EnchantmentKeys
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys
import io.papermc.paper.registry.tag.TagKey
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup

class WakameEnchantments(
    private val context: BootstrapContext,
) {
    companion object {
        private val _ALL = ObjectArraySet<CustomEnchantment>()

        private fun createEnchantment(
            enchantmentKey: TypedKey<Enchantment>,
            tags: Collection<TagKey<Enchantment>>,
            builder: EnchantmentRegistryEntry.Builder.(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>, TypedKey<Enchantment>) -> Unit,
        ): CustomEnchantment {
            val enchantment = CustomEnchantment(enchantmentKey, tags, builder)
            _ALL.add(enchantment)
            return enchantment
        }

        val ALL: Set<CustomEnchantment>
            get() = _ALL

        val UNIVERSAL_INCOMING_DAMAGE_RATE: CustomEnchantment = createEnchantment(
            enchantmentKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("wakame", "universal_incoming_damage_rate")),
            tags = listOf(
                *WakameEnchantmentsSupport.VANILLA_BANNED_TAGS,
                EnchantmentTagKeys.EXCLUSIVE_SET_ARMOR
            ),
        ) { event, enchantmentKey ->
            applyCommonProperties(enchantmentKey, 4)
            protectionProperties(event)
            supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
            anvilCost(1)
            weight(10)
            minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
            maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
        }

        val FIRE_INCOMING_DAMAGE_RATE: CustomEnchantment = createEnchantment(
            enchantmentKey = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key("wakame", "incoming_damage_rate/fire")),
            tags = listOf(
                *WakameEnchantmentsSupport.VANILLA_BANNED_TAGS,
                EnchantmentTagKeys.EXCLUSIVE_SET_ARMOR
            ),
        ) { event, enchantmentKey ->
            applyCommonProperties(enchantmentKey, 4)
            protectionProperties(event)
            supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
            anvilCost(1)
            weight(10)
            minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 1))
            maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 1))
        }

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

    fun register() {
        val manager = context.lifecycleManager
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze()) {
            for (enchantment in ALL) {
                enchantment.register(it)
            }
        }

        registerCustomEnchantmentTags()

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd()
            .newHandler { event ->
                event.builder()
                    .description(
                        Component.text()
                            .content("DO_NOT_USE")
                            .color(NamedTextColor.RED)
                            .append(Component.text("-"))
                            .append(Component.translatable(getTranslateKey(event.key())).color(NamedTextColor.GRAY))
                            .build()
                    )
            }
            .filter { !isAllowedEnchantment(it) }
        )
    }

    private fun registerCustomEnchantmentTags() {
        context.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event ->
            val registrar = event.registrar()
            WakameEnchantmentsSupport.VANILLA_BANNED_TAGS.forEach {
                val enchantments = registrar.getTag(it)
                registrar.setTag(it, enchantments.filter(::isAllowedEnchantment))
            }
            // Clear enchantment banned tags
            for (customEnchantment in ALL) {
                customEnchantment.tags.forEach { registrar.addToTag(it, listOf(customEnchantment.enchantmentKey)) }
            }
        }
    }

    private fun isAllowedEnchantment(enchantmentKey: TypedKey<Enchantment>): Boolean {
        if (ALL.any { it.enchantmentKey == enchantmentKey }) {
            // 自定义附魔肯定是允许的
            return true
        }

        if (enchantmentKey in WakameEnchantmentsSupport.ALLOWED_ENCHANTMENTS) {
            // 在允许的附魔内
            return true
        }

        return false
    }
}

private object WakameEnchantmentsSupport {
    // 当这些标签从附魔上移除时，附魔将起到被禁用的效果 (即无法通过生存模式获取)
    val VANILLA_BANNED_TAGS = arrayOf(
        EnchantmentTagKeys.IN_ENCHANTING_TABLE,
        EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT,
        EnchantmentTagKeys.ON_RANDOM_LOOT,
        EnchantmentTagKeys.ON_TRADED_EQUIPMENT,
        EnchantmentTagKeys.TRADEABLE
    )

    // 允许的附魔
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

    fun getTranslateKey(key: TypedKey<*>): String {
        val enchantmentKey = key.key()
        return "enchantment.${enchantmentKey.namespace()}.${enchantmentKey.value()}"
    }

    fun EnchantmentRegistryEntry.Builder.applyCommonProperties(
        enchantmentKey: TypedKey<Enchantment>,
        maxLevel: Int = 1,
    ) {
        description(Component.translatable(getTranslateKey(enchantmentKey)))
        maxLevel(maxLevel)
    }

    fun EnchantmentRegistryEntry.Builder.protectionProperties(
        event: RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    ) {
        activeSlots(EquipmentSlotGroup.ARMOR)
        exclusiveWith(event.getOrCreateTag(EnchantmentTagKeys.EXCLUSIVE_SET_ARMOR))
    }
}