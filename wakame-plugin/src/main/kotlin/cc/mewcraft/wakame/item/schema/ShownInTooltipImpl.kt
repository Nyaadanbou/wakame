@file:OptIn(ExperimentalContracts::class)

package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import com.google.common.collect.ImmutableMultimap
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import org.bukkit.inventory.meta.ItemMeta as BukkitItemMeta

/**
 * 该接口为纯标记用途。
 */
internal interface BukkitShownInTooltip : ShownInTooltip {
    fun hide(meta: BukkitItemMeta)
    fun show(meta: BukkitItemMeta)

    override fun hide(any: Any) {
        ensureType(any)
        hide(any)
    }

    override fun show(any: Any) {
        ensureType(any)
        show(any)
    }

    private fun ensureType(item: Any) {
        contract { returns() implies (item is BukkitItemMeta) }
        require(item is BukkitItemMeta) { "The item must be an org.bukkit.inventory.meta.ItemMeta, but was ${item::class.qualifiedName}" }
    }
}

/**
 * [ShownInTooltip] 的所有具体实现。使用枚举类实现。
 */
internal enum class BukkitShownInTooltips(
    override val component: Key,
) : BukkitShownInTooltip {
    TRIM("trim") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_ARMOR_TRIM)
        }
    },
    ATTRIBUTE_MODIFIERS("attribute_modifiers") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            if (!meta.hasAttributeModifiers()) {
                // 1.20.5+ Bukkit API 要求必须定义属性修饰符（哪怕为空）才能控制显示与否
                meta.attributeModifiers = ImmutableMultimap.of()
            }
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            if (!meta.hasAttributeModifiers()) {
                // 1.20.5+ Bukkit API 要求必须定义属性修饰符（哪怕为空）才能控制显示与否
                meta.attributeModifiers = ImmutableMultimap.of()
            }
        }
    },
    CAN_BREAK("can_break") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_DESTROYS)
        }
    },
    DYED_COLOR("dyed_color") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_DYE)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_DYE)
        }
    },
    ENCHANTMENTS("enchantments") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
    },
    CAN_PLACE_ON("can_place_on") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        }
    },
    STORED_ENCHANTMENTS("stored_enchantments") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_STORED_ENCHANTS)
        }
    },
    UNBREAKABLE("unbreakable") {
        override fun hide(meta: ItemMeta) {
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        }

        override fun show(meta: ItemMeta) {
            meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        }
    },
    ;

    constructor(component: String) : this(Key(component))

    override fun toString(): String {
        return toSimpleString()
    }

    companion object {
        private val COMPONENT_TO_BUKKIT_SHOWN_IN_TOOLTIP: Map<Key, BukkitShownInTooltip> = buildMap {
            BukkitShownInTooltips.entries.forEach { entry: BukkitShownInTooltips -> put(entry.component, entry) }
        }

        fun getShownInTooltipByComponent(component: String): BukkitShownInTooltip {
            return requireNotNull(COMPONENT_TO_BUKKIT_SHOWN_IN_TOOLTIP[Key(component)]) { "Can't find BukkitShownInTooltip for item component name $component" }
        }
    }
}

internal class BukkitShownInTooltipApplicator(
    private val settings: Map<BukkitShownInTooltip, Boolean>,
) : ShownInTooltipApplicator {
    override fun apply(any: Any) {
        ensureType(any)
        for ((shownInTooltip: BukkitShownInTooltip, isShow: Boolean) in settings) {
            if (isShow) {
                shownInTooltip.show(any)
            } else {
                shownInTooltip.hide(any)
            }
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("settings", settings))
    }

    override fun toString(): String {
        return toSimpleString()
    }

    private fun ensureType(any: Any) {
        require(any is BukkitItemMeta) { "The item must be an org.bukkit.inventory.meta.ItemMeta, but was ${any::class.qualifiedName}" }
    }
}

/**
 * This is a serializer for [BukkitShownInTooltipApplicator].
 *
 * ## Expected node structure
 *
 * ```yaml
 * <node>:
 *   trim: false
 *   attribute_modifiers: false
 *   can_break: false
 *   dyed_color: false
 *   enchantments: false
 *   can_place_on: false
 *   stored_enchantments: false
 *   unbreakable: false
 * ```
 */
internal object BukkitShownInTooltipApplicatorSerializer : ShownInTooltipApplicatorSerializer {
    override fun deserialize(type: Type, node: ConfigurationNode): ShownInTooltipApplicator {
        if (!node.isMap) throw SerializationException()

        val settings = node.childrenMap()
            // map key objects to their string representations
            .mapKeys {
                it.key.toString()
            }
            // map strings to BukkitShownInTooltip instances
            .mapKeys {
                BukkitShownInTooltips.getShownInTooltipByComponent(it.key)
            }
            // map value nodes to boolean values
            .mapValues {
                // `true`  = show it
                // `false` = hide it
                // we hide it by default, so `false`
                it.value.getBoolean(false)
            }

        return BukkitShownInTooltipApplicator(settings)
    }

    override fun serialize(type: Type, obj: ShownInTooltipApplicator?, node: ConfigurationNode?) {
        throw UnsupportedOperationException()
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ShownInTooltipApplicator {
        // return a non-null value to allow missing value
        return BukkitShownInTooltipApplicator(emptyMap())
    }
}