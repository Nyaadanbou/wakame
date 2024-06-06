package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream
import org.bukkit.inventory.ItemFlag as BukkitItemFlag

/**
 * 该接口为纯标记用途。
 */
internal interface BukkitShownInTooltip : ShownInTooltip

/**
 * [ShownInTooltip] 的所有具体实现。使用枚举类实现。
 */
internal enum class BukkitShownInTooltips(
    override val component: Key,
) : BukkitShownInTooltip {
    TRIM("trim"),
    ATTRIBUTE_MODIFIERS("attribute_modifiers"),
    CAN_BREAK("can_break"),
    DYED_COLOR("dyed_color"),
    ENCHANTMENTS("enchantments"),
    CAN_PLACE_ON("can_place_on"),
    STORED_ENCHANTMENTS("stored_enchantments"),
    UNBREAKABLE("unbreakable"),
    ;

    constructor(component: String) : this(Key(component))

    override fun show(item: Any) {
        require(item is ItemStack) { "The item must be an org.bukkit.ItemStack, but was ${item::class.qualifiedName}" }
        item.removeItemFlags(getItemFlag())
    }

    override fun hide(item: Any) {
        require(item is ItemStack) { "The item must be an org.bukkit.ItemStack, but was ${item::class.qualifiedName}" }
        item.addItemFlags(getItemFlag())
    }

    override fun toString(): String {
        return toSimpleString()
    }

    private fun getItemFlag(): BukkitItemFlag {
        return getItemFlagByComponent(this.component.asString())
    }

    companion object {
        private val COMPONENT_TO_BUKKIT_ITEM_FLAG: Map<Key, BukkitItemFlag> = buildMap {
            fun create(minimalComponentName: String, itemFlag: BukkitItemFlag) {
                put(Key(minimalComponentName), itemFlag)
            }

            create("trim", BukkitItemFlag.HIDE_ARMOR_TRIM)
            create("attribute_modifiers", BukkitItemFlag.HIDE_ATTRIBUTES)
            create("can_break", BukkitItemFlag.HIDE_DESTROYS)
            create("dyed_color", BukkitItemFlag.HIDE_DYE)
            create("enchantments", BukkitItemFlag.HIDE_ENCHANTS)
            create("can_place_on", BukkitItemFlag.HIDE_PLACED_ON)
            create("stored_enchantments", BukkitItemFlag.HIDE_STORED_ENCHANTS)
            create("unbreakable", BukkitItemFlag.HIDE_UNBREAKABLE)
        }

        private val COMPONENT_TO_BUKKIT_SHOWN_IN_TOOLTIP: Map<Key, BukkitShownInTooltip> = buildMap {
            fun create(minimalComponentName: String, bukkitShownInTooltip: BukkitShownInTooltip) {
                put(Key(minimalComponentName), bukkitShownInTooltip)
            }

            create("trim", TRIM)
            create("attribute_modifiers", ATTRIBUTE_MODIFIERS)
            create("can_break", CAN_BREAK)
            create("dyed_color", DYED_COLOR)
            create("enchantments", ENCHANTMENTS)
            create("can_place_on", CAN_PLACE_ON)
            create("stored_enchantments", STORED_ENCHANTMENTS)
            create("unbreakable", UNBREAKABLE)
        }

        fun getItemFlagByComponent(component: String): BukkitItemFlag {
            return requireNotNull(COMPONENT_TO_BUKKIT_ITEM_FLAG[Key(component)]) { "Can't find BukkitItemFlag for item component name $component" }
        }

        fun getShownInTooltipByComponent(component: String): BukkitShownInTooltip {
            return requireNotNull(COMPONENT_TO_BUKKIT_SHOWN_IN_TOOLTIP[Key(component)]) { "Can't find BukkitShownInTooltip for item component name $component" }
        }
    }
}

internal class BukkitShownInTooltipApplicator(
    private val settings: Map<BukkitShownInTooltip, Boolean>,
) : ShownInTooltipApplicator {
    override fun applyToItem(item: Any) {
        require(item is ItemStack) { "The item must be an org.bukkit.ItemStack, but was ${item::class.qualifiedName}" }
        for ((flag, show) in settings) {
            if (show) {
                flag.show(item)
            } else {
                flag.hide(item)
            }
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(ExaminableProperty.of("settings", settings))
    }

    override fun toString(): String {
        return toSimpleString()
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
            .mapKeys { it.key.toString() }
            // map strings to BukkitShownInTooltip instances
            .mapKeys { BukkitShownInTooltips.getShownInTooltipByComponent(it.key) }
            // map value nodes to boolean values
            .mapValues { it.value.getBoolean(false) } // `add` ItemFlag by default

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