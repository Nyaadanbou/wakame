@file:Suppress("DuplicatedCode")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType
import cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.contains
import java.nio.file.Path

internal class RestrictedItemTemplateException : Throwable()
internal class UnsupportedItemTemplateException : Throwable()

internal class VanillaItemTemplateValidator {
    fun restricted(predicate: () -> Boolean) {
        if (predicate()) {
            throw RestrictedItemTemplateException()
        }
    }

    fun unsupported() {
        throw UnsupportedItemTemplateException()
    }
}

object NekoItemFactory : KoinComponent {
    fun createVanilla(key: Key, relPath: Path, root: ConfigurationNode): NekoItem {
        // read all basic info
        val itemBase = root.node("base").krequire<ItemBase>()
        val slotGroup = root.node("slot").krequire<ItemSlotGroup>()

        // read all item behaviors
        val behaviorMap = ItemBehaviorMap.build {
            fun <T : ItemBehavior> tryAdd(path: String, type: ItemBehaviorType<T>) {
                // 如果 root 里存在指定 id 的节点, 则添加对应的 behavior
                if (root.contains(path)) this.put(type, type.create())
            }

            tryAdd("attack", ItemBehaviorTypes.ATTACK)
            tryAdd("castable", ItemBehaviorTypes.CASTABLE)
            // tryAdd("chargeable", ItemBehaviorTypes.CHARGEABLE)
            // tryAdd("damageable", ItemBehaviorTypes.DAMAGEABLE)
            // tryAdd("enchantable", ItemBehaviorTypes.ENCHANTABLE)
            // tryAdd("food", ItemBehaviorTypes.FOOD)
            tryAdd("hold_last_damage", ItemBehaviorTypes.HOLD_LAST_DAMAGE)
            tryAdd("level_barrier", ItemBehaviorTypes.LEVEL_BARRIER)
            // tryAdd("tool", ItemBehaviorTypes.TOOL)
            // tryAdd("trackable", ItemBehaviorTypes.TRACKABLE)
            // tryAdd("wearable", ItemBehaviorTypes.WEARABLE)
        }

        // read all item templates (of item components)
        val templateMap = ItemTemplateMap.build {
            fun <T : ItemTemplate<*>> tryAdd(
                path: String,
                type: ItemTemplateType<T>,
                block: VanillaItemTemplateValidator.(T) -> Unit = {},
            ) {
                val node = root.node(path)
                if (node.virtual()) return // 如果节点是虚拟的, 则直接返回
                val template = type.decode(node)
                try {
                    block(VanillaItemTemplateValidator(), template)
                } catch (e: UnsupportedItemTemplateException) {
                    if (RunningEnvironment.TEST.isRunning()) {
                        throw e
                    } else {
                        logger.warn("Unsupported template ('${type.id}') for item type: '${key.asString()}'")
                    }
                } catch (e: RestrictedItemTemplateException) {
                    if (RunningEnvironment.TEST.isRunning()) {
                        throw e
                    } else {
                        logger.warn("Invalid template ('${type.id}') for item type: '${key.asString()}'")
                        return
                    }
                }
                this.put(type, template)
            }

            tryAdd("arrow", ItemTemplateTypes.ARROW)
            tryAdd("attack", ItemTemplateTypes.ATTACK)
            tryAdd("attribute_modifiers", ItemTemplateTypes.ATTRIBUTE_MODIFIERS) { unsupported() }
            tryAdd("can_break", ItemTemplateTypes.CAN_BREAK) { unsupported() }
            tryAdd("can_place_on", ItemTemplateTypes.CAN_PLACE_ON) { unsupported() }
            tryAdd("castable", ItemTemplateTypes.CASTABLE)
            tryAdd("cells", ItemTemplateTypes.CELLS)
            tryAdd("crate", ItemTemplateTypes.CRATE) { unsupported() }
            tryAdd("custom_name", ItemTemplateTypes.CUSTOM_NAME) { unsupported() }
            tryAdd("damage", ItemTemplateTypes.DAMAGE) { unsupported() }
            tryAdd("dyed_color", ItemTemplateTypes.DYED_COLOR) { unsupported() }
            tryAdd("elements", ItemTemplateTypes.ELEMENTS)
            tryAdd("enchantments", ItemTemplateTypes.ENCHANTMENTS) { unsupported() }
            tryAdd("fire_resistant", ItemTemplateTypes.FIRE_RESISTANT) { unsupported() }
            tryAdd("food", ItemTemplateTypes.FOOD) { unsupported() }
            tryAdd("glowable", ItemTemplateTypes.GLOWABLE)
            tryAdd("hide_tooltip", ItemTemplateTypes.HIDE_TOOLTIP) { unsupported() }
            tryAdd("hide_additional_tooltip", ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP) { unsupported() }
            tryAdd("item_name", ItemTemplateTypes.ITEM_NAME) { unsupported() }
            tryAdd("kizamiz", ItemTemplateTypes.KIZAMIZ)
            tryAdd("level", ItemTemplateTypes.LEVEL) { restricted { !it.isConstant } } // 对于 VanillaNekoItem, LEVEL 应该始终采用固定值
            tryAdd("lore", ItemTemplateTypes.LORE)
            tryAdd("max_damage", ItemTemplateTypes.MAX_DAMAGE) { unsupported() }
            tryAdd("portable_core", ItemTemplateTypes.PORTABLE_CORE) { unsupported() }
            tryAdd("rarity", ItemTemplateTypes.RARITY) { restricted { !it.isStatic } } // 对于 VanillaNekoItem, RARITY 应该始终采用固定值
            // tryAdd("skin", ItemTemplateTypes.SKIN) { unsupported() }
            // tryAdd("skin_owner", ItemTemplateTypes.SKIN_OWNER) { unsupported() }
            tryAdd("stored_enchantments", ItemTemplateTypes.STORED_ENCHANTMENTS) { unsupported() }
            tryAdd("tool", ItemTemplateTypes.TOOL) { unsupported() }
            tryAdd("trim", ItemTemplateTypes.TRIM) { unsupported() }
            tryAdd("unbreakable", ItemTemplateTypes.UNBREAKABLE) { unsupported() }
        }

        return SimpleNekoItem(
            id = key,
            base = itemBase,
            slotGroup = slotGroup,
            templates = templateMap,
            behaviors = behaviorMap
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

            tryAdd("attack", ItemBehaviorTypes.ATTACK)
            tryAdd("castable", ItemBehaviorTypes.CASTABLE)
            tryAdd("chargeable", ItemBehaviorTypes.CHARGEABLE)
            tryAdd("max_damage", ItemBehaviorTypes.MAX_DAMAGE)
            tryAdd("enchantable", ItemBehaviorTypes.ENCHANTABLE)
            tryAdd("food", ItemBehaviorTypes.FOOD)
            tryAdd("level_barrier", ItemBehaviorTypes.LEVEL_BARRIER)
            tryAdd("tool", ItemBehaviorTypes.TOOL)
            tryAdd("trackable", ItemBehaviorTypes.TRACKABLE)
            tryAdd("wearable", ItemBehaviorTypes.WEARABLE)
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
            tryAdd("fire_resistant", ItemTemplateTypes.FIRE_RESISTANT)
            tryAdd("food", ItemTemplateTypes.FOOD)
            tryAdd("glowable", ItemTemplateTypes.GLOWABLE)
            tryAdd("hide_tooltip", ItemTemplateTypes.HIDE_TOOLTIP)
            tryAdd("hide_additional_tooltip", ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP)
            tryAdd("item_name", ItemTemplateTypes.ITEM_NAME)
            tryAdd("kizamiz", ItemTemplateTypes.KIZAMIZ)
            tryAdd("level", ItemTemplateTypes.LEVEL)
            tryAdd("lore", ItemTemplateTypes.LORE)
            tryAdd("max_damage", ItemTemplateTypes.MAX_DAMAGE)
            tryAdd("portable_core", ItemTemplateTypes.PORTABLE_CORE)
            tryAdd("rarity", ItemTemplateTypes.RARITY)
            // tryAdd("skin", ItemTemplateTypes.SKIN)
            // tryAdd("skin_owner", ItemTemplateTypes.SKIN_OWNER)
            tryAdd("stored_enchantments", ItemTemplateTypes.STORED_ENCHANTMENTS)
            tryAdd("tool", ItemTemplateTypes.TOOL)
            tryAdd("trim", ItemTemplateTypes.TRIM)
            tryAdd("unbreakable", ItemTemplateTypes.UNBREAKABLE)
        }

        return SimpleNekoItem(
            id = key,
            base = itemBase,
            slotGroup = slotGroup,
            templates = templateMap,
            behaviors = behaviorMap
        )
    }

    private val logger by inject<Logger>()
}