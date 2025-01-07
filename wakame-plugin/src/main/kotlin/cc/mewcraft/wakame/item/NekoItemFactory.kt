@file:Suppress("DuplicatedCode")

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
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.contains
import java.nio.file.Path

object NekoItemFactory {
    fun createVanilla(key: Key, relPath: Path, root: ConfigurationNode): NekoItem {
        // read all basic info
        val itemBase = root.node("base").krequire<ItemBase>()
        val slotGroup = root.node("slot").krequire<ItemSlotGroup>()

        // read all item behaviors
        val behaviorMap = ItemBehaviorMap.build {
            fun <T : ItemBehavior> tryAdd(path: String, type: ItemBehaviorType<T>) {
                // 如果 root 里存在指定 id 的节点, 则添加对应的 behavior
                if (root.contains(path)) {
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

        // read all item templates (of item components)
        val templateMap = ItemTemplateMap.build {
            @Suppress("UNCHECKED_CAST")
            fun <T : ItemTemplate<*>> tryAdd(
                path: String,
                type: ItemTemplateType<T>,
                validator: Validator<T> = Validator.None as Validator<T>,
            ) {
                val node = root.node(path)
                if (node.virtual()) {
                    return // 如果节点是虚拟的, 则直接返回
                }
                val template = type.decode(node)
                try {
                    validator.validate(ValidatorContext(key, type, template))
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
            id = key, base = itemBase, slotGroup = slotGroup, templates = templateMap, behaviors = behaviorMap
        )
    }

    fun createCustom(key: Key, relPath: Path, root: ConfigurationNode): NekoItem {
        return create(key, relPath, root)
    }

    /**
     * Creates a [NekoItem] from a [configuration node][ConfigurationNode].
     *
     * @param key the key of the item
     * @param relPath the relative path of the item in the configuration
     * @param root the configuration node holding the data of the item
     * @return a new [NekoItem]
     */
    fun create(key: Key, relPath: Path, root: ConfigurationNode): NekoItem {
        // read all basic info
        val itemBase = root.node("base").krequire<ItemBase>()
        val slotGroup = root.node("slot").krequire<ItemSlotGroup>()

        // read all item behaviors
        val behaviorMap = ItemBehaviorMap.build {
            fun <T : ItemBehavior> tryAdd(path: String, type: ItemBehaviorType<T>) {
                if (root.contains(path)) this.put(type, type.create())
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

        // read all item templates (of item components)
        val templateMap = ItemTemplateMap.build {
            fun <T : ItemTemplate<*>> tryAdd(path: String, type: ItemTemplateType<T>) {
                val node = root.node(path)
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
            tryAdd("menu_icon_dict", ItemTemplateTypes.MENU_ICON_DICT)
            tryAdd("menu_icon_lore", ItemTemplateTypes.MENU_ICON_LORE)
            tryAdd("menu_icon_name", ItemTemplateTypes.MENU_ICON_NAME)
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
            id = key, base = itemBase, slotGroup = slotGroup, templates = templateMap, behaviors = behaviorMap
        )
    }
}

internal class RestrictedItemTemplateException(override val message: String?, override val cause: Throwable? = null) : Throwable()

internal class UnsupportedItemTemplateException(override val message: String?, override val cause: Throwable? = null) : Throwable()

private sealed interface Validator<T> {

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T> none(): Validator<T> = None as Validator<T>
        fun <T> unsupported(): Validator<T> = Unsupported as Validator<T>
        fun <T> restricted(test: (T) -> Boolean): Validator<T> = Restricted(test)
    }

    fun validate(context: ValidatorContext<T>)

    object None : Validator<Nothing> {
        override fun validate(context: ValidatorContext<Nothing>) = Unit
    }

    object Unsupported : Validator<Nothing> {
        override fun validate(context: ValidatorContext<Nothing>) {
            throw UnsupportedItemTemplateException("Adding unsupported config '${context.type.id} to item type '${context.item}''")
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

private class ValidatorContext<T>(val item: Key, val type: ItemTemplateType<*>, val data: T)
