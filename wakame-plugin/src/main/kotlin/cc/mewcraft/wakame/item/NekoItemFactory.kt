package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.Util
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.contains

object NekoItemFactory {
    val VANILLA: VanillaNekoItemFactory = VanillaNekoItemFactory
    val STANDARD: StandardNekoItemFactory = StandardNekoItemFactory
}

object VanillaNekoItemFactory {
    operator fun invoke(id: Identifier, rootNode: ConfigurationNode): NekoItem {
        // read basic data
        val itemBase = rootNode.node("base").require<ItemBase>()
        val slotGroup = rootNode.node("slot").require<ItemSlotGroup>()

        // read item behaviors
        val behaviorMap = ItemBehaviorMap.build {
            fun <T : ItemBehavior> tryAdd(path: String, type: ItemBehaviorType<T>) {
                // 如果 rootNode 里存在指定 id 的节点, 则添加对应的 behavior
                if (rootNode.contains(path)) {
                    this.put(type, type.create())
                }
            }

            tryAdd("level_barrier", ItemBehaviorTypes.LEVEL_BARRIER)
            tryAdd("hold_last_damage", ItemBehaviorTypes.HOLD_LAST_DAMAGE)
            tryAdd("arrow", ItemBehaviorTypes.ARROW)
            tryAdd("attack", ItemBehaviorTypes.ATTACK)
            tryAdd("castable", ItemBehaviorTypes.CASTABLE)
            // tryAdd("chargeable", ItemBehaviorTypes.CHARGEABLE)
            // tryAdd("enchantable", ItemBehaviorTypes.ENCHANTABLE)
            // tryAdd("food", ItemBehaviorTypes.FOOD)
            // tryAdd("tool", ItemBehaviorTypes.TOOL)
            tryAdd("town_flight", ItemBehaviorTypes.TOWN_FLIGHT)
            // tryAdd("trackable", ItemBehaviorTypes.TRACKABLE)
            // tryAdd("wearable", ItemBehaviorTypes.WEARABLE)
            tryAdd("world_time_control", ItemBehaviorTypes.WORLD_TIME_CONTROL)
            tryAdd("world_weather_control", ItemBehaviorTypes.WORLD_WEATHER_CONTROL)
        }

        // read item templates
        val templateMap = ItemTemplateMap.build {
            fun <T : ItemTemplate<*>> tryAdd(
                path: String,
                type: ItemTemplateType<T>,
                validator: Validator<T> = Validator.none(), // 默认无验证
            ) {
                val node = rootNode.node(path)
                if (node.virtual()) {
                    return // 如果节点是虚拟的, 则直接返回
                }
                val template = type.decode(node)
                try {
                    validator.validate(ValidatorContext(id, type, template))
                } catch (e: UnsupportedItemTemplateException) {
                    Util.pauseInIde(e)
                } catch (e: RestrictedItemTemplateException) {
                    Util.pauseInIde(e)
                }
                this.put(type, template)
            }

            tryAdd("arrow", ItemTemplateTypes.ARROW)
            tryAdd("attack", ItemTemplateTypes.ATTACK)
            tryAdd("attack_speed", ItemTemplateTypes.ATTACK_SPEED)
            tryAdd("attribute_modifiers", ItemTemplateTypes.ATTRIBUTE_MODIFIERS, Validator.unsupported())
            tryAdd("can_break", ItemTemplateTypes.CAN_BREAK, Validator.unsupported())
            tryAdd("can_place_on", ItemTemplateTypes.CAN_PLACE_ON, Validator.unsupported())
            tryAdd("castable", ItemTemplateTypes.CASTABLE)
            tryAdd("cells", ItemTemplateTypes.CELLS)
            tryAdd("crate", ItemTemplateTypes.CRATE, Validator.unsupported())
            tryAdd("custom_name", ItemTemplateTypes.CUSTOM_NAME, Validator.unsupported())
            tryAdd("damage", ItemTemplateTypes.DAMAGE, Validator.unsupported())
            tryAdd("dyed_color", ItemTemplateTypes.DYED_COLOR, Validator.unsupported())
            tryAdd("elements", ItemTemplateTypes.ELEMENTS)
            tryAdd("enchantments", ItemTemplateTypes.ENCHANTMENTS, Validator.unsupported())
            tryAdd("damage_resistant", ItemTemplateTypes.DAMAGE_RESISTANT, Validator.unsupported())
            tryAdd("food", ItemTemplateTypes.FOOD, Validator.unsupported())
            tryAdd("glowable", ItemTemplateTypes.GLOWABLE)
            tryAdd("hide_tooltip", ItemTemplateTypes.HIDE_TOOLTIP, Validator.unsupported())
            tryAdd("hide_additional_tooltip", ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP, Validator.unsupported())
            tryAdd("item_name", ItemTemplateTypes.ITEM_NAME, Validator.unsupported())
            tryAdd("kizamiz", ItemTemplateTypes.KIZAMIZ)
            tryAdd("level", ItemTemplateTypes.LEVEL, Validator.restricted { cfg -> cfg.isConstant }) // LEVEL 必须使用固定值
            tryAdd("lore", ItemTemplateTypes.LORE)
            tryAdd("max_damage", ItemTemplateTypes.MAX_DAMAGE, Validator.unsupported())
            tryAdd("portable_core", ItemTemplateTypes.PORTABLE_CORE, Validator.unsupported())
            tryAdd("rarity", ItemTemplateTypes.RARITY, Validator.restricted { cfg -> cfg.isStatic }) // RARITY 必须使用固定值
            // tryAdd("skin", ItemTemplateTypes.SKIN)
            // tryAdd("skin_owner", ItemTemplateTypes.SKIN_OWNER)
            tryAdd("stored_enchantments", ItemTemplateTypes.STORED_ENCHANTMENTS, Validator.unsupported())
            tryAdd("tool", ItemTemplateTypes.TOOL, Validator.unsupported())
            tryAdd("town_flight", ItemTemplateTypes.TOWN_FLIGHT)
            tryAdd("trim", ItemTemplateTypes.TRIM, Validator.unsupported())
            tryAdd("unbreakable", ItemTemplateTypes.UNBREAKABLE, Validator.unsupported())
            tryAdd("world_time_control", ItemTemplateTypes.WORLD_TIME_CONTROL)
            tryAdd("world_weather_control", ItemTemplateTypes.WORLD_WEATHER_CONTROL)
        }

        return SimpleNekoItem(
            id = id,
            base = itemBase,
            slotGroup = slotGroup,
            hidden = true,
            templates = templateMap,
            behaviors = behaviorMap
        )
    }
}

object StandardNekoItemFactory {
    /**
     * Creates a [NekoItem] from a [ConfigurationNode].
     *
     * @param id the key of the item
     * @param rootNode the configuration node holding the data of the item
     * @return a new [NekoItem]
     */
    operator fun invoke(id: Identifier, rootNode: ConfigurationNode): NekoItem {
        // read basic data
        val itemBase = rootNode.node("base").require<ItemBase>()
        val slotGroup = rootNode.node("slot").require<ItemSlotGroup>()

        // read item behaviors
        val behaviorMap = ItemBehaviorMap.build {
            fun <T : ItemBehavior> tryAdd(path: String, type: ItemBehaviorType<T>) {
                if (rootNode.contains(path)) this.put(type, type.create())
            }

            tryAdd("level_barrier", ItemBehaviorTypes.LEVEL_BARRIER)
            tryAdd("hold_last_damage", ItemBehaviorTypes.HOLD_LAST_DAMAGE)
            tryAdd("arrow", ItemBehaviorTypes.ARROW)
            tryAdd("attack", ItemBehaviorTypes.ATTACK)
            tryAdd("castable", ItemBehaviorTypes.CASTABLE)
            tryAdd("chargeable", ItemBehaviorTypes.CHARGEABLE)
            tryAdd("enchantable", ItemBehaviorTypes.ENCHANTABLE)
            tryAdd("food", ItemBehaviorTypes.FOOD)
            tryAdd("tool", ItemBehaviorTypes.TOOL)
            tryAdd("town_flight", ItemBehaviorTypes.TOWN_FLIGHT)
            tryAdd("trackable", ItemBehaviorTypes.TRACKABLE)
            tryAdd("wearable", ItemBehaviorTypes.WEARABLE)
            tryAdd("world_time_control", ItemBehaviorTypes.WORLD_TIME_CONTROL)
            tryAdd("world_weather_control", ItemBehaviorTypes.WORLD_WEATHER_CONTROL)
        }

        // read item templates
        val templateMap = ItemTemplateMap.build {
            fun <T : ItemTemplate<*>> tryAdd(path: String, type: ItemTemplateType<T>) {
                val node = rootNode.node(path)
                if (node.virtual()) return
                val template = type.decode(node)
                this.put(type, template)
            }

            tryAdd("arrow", ItemTemplateTypes.ARROW)
            tryAdd("attack", ItemTemplateTypes.ATTACK)
            tryAdd("attack_speed", ItemTemplateTypes.ATTACK_SPEED)
            tryAdd("attribute_modifiers", ItemTemplateTypes.ATTRIBUTE_MODIFIERS)
            tryAdd("can_break", ItemTemplateTypes.CAN_BREAK)
            tryAdd("can_place_on", ItemTemplateTypes.CAN_PLACE_ON)
            tryAdd("castable", ItemTemplateTypes.CASTABLE)
            tryAdd("cells", ItemTemplateTypes.CELLS)
            tryAdd("crate", ItemTemplateTypes.CRATE)
            tryAdd("custom_name", ItemTemplateTypes.CUSTOM_NAME)
            tryAdd("damage", ItemTemplateTypes.DAMAGE)
            tryAdd("dyed_color", ItemTemplateTypes.DYED_COLOR)
            tryAdd("elements", ItemTemplateTypes.ELEMENTS)
            tryAdd("enchantments", ItemTemplateTypes.ENCHANTMENTS)
            tryAdd("damage_resistant", ItemTemplateTypes.DAMAGE_RESISTANT)
            tryAdd("food", ItemTemplateTypes.FOOD)
            tryAdd("glowable", ItemTemplateTypes.GLOWABLE)
            tryAdd("hide_tooltip", ItemTemplateTypes.HIDE_TOOLTIP)
            tryAdd("hide_additional_tooltip", ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP)
            tryAdd("item_name", ItemTemplateTypes.ITEM_NAME)
            tryAdd("kizamiz", ItemTemplateTypes.KIZAMIZ)
            tryAdd("level", ItemTemplateTypes.LEVEL)
            tryAdd("lore", ItemTemplateTypes.LORE)
            tryAdd("max_damage", ItemTemplateTypes.MAX_DAMAGE)
            tryAdd("slot_display_dict", ItemTemplateTypes.SLOT_DISPLAY_DICT)
            tryAdd("slot_display_lore", ItemTemplateTypes.SLOT_DISPLAY_LORE)
            tryAdd("slot_display_name", ItemTemplateTypes.SLOT_DISPLAY_NAME)
            tryAdd("portable_core", ItemTemplateTypes.PORTABLE_CORE)
            tryAdd("rarity", ItemTemplateTypes.RARITY)
            // tryAdd("skin", ItemTemplateTypes.SKIN)
            // tryAdd("skin_owner", ItemTemplateTypes.SKIN_OWNER)
            tryAdd("stored_enchantments", ItemTemplateTypes.STORED_ENCHANTMENTS)
            tryAdd("tool", ItemTemplateTypes.TOOL)
            tryAdd("town_flight", ItemTemplateTypes.TOWN_FLIGHT)
            tryAdd("trim", ItemTemplateTypes.TRIM)
            tryAdd("unbreakable", ItemTemplateTypes.UNBREAKABLE)
            tryAdd("world_time_control", ItemTemplateTypes.WORLD_TIME_CONTROL)
            tryAdd("world_weather_control", ItemTemplateTypes.WORLD_WEATHER_CONTROL)
        }

        return SimpleNekoItem(
            id = id,
            base = itemBase,
            slotGroup = slotGroup,
            hidden = false,
            templates = templateMap,
            behaviors = behaviorMap
        )
    }
}

internal class RestrictedItemTemplateException(override val message: String?, override val cause: Throwable? = null) : Throwable()

internal class UnsupportedItemTemplateException(override val message: String?, override val cause: Throwable? = null) : Throwable()

private sealed interface Validator<T> {

    companion object {
        fun <T> none(): Validator<T> = None as Validator<T>
        fun <T> unsupported(): Validator<T> = Unsupported as Validator<T>
        fun <T> restricted(test: (T) -> Boolean): Validator<T> = Restricted(test)
    }

    fun validate(context: ValidatorContext<T>)

    data object None : Validator<Nothing> {
        override fun validate(context: ValidatorContext<Nothing>) = Unit
    }

    data object Unsupported : Validator<Nothing> {
        override fun validate(context: ValidatorContext<Nothing>) {
            throw UnsupportedItemTemplateException("Adding unsupported config '${context.type.id}' to item type '${context.item}''")
        }
    }

    class Restricted<T>(private val test: (T) -> Boolean) : Validator<T> {
        override fun validate(context: ValidatorContext<T>) {
            if (!test(context.data)) {
                throw RestrictedItemTemplateException("Adding restricted config '${context.type.id}' to item type '${context.item}'")
            }
        }
    }
}

private class ValidatorContext<T>(val item: Identifier, val type: ItemTemplateType<*>, val data: T)
