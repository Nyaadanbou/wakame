package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * A wrapper of an ItemStack, which provides dedicated properties and
 * functions to manipulate wakame data on the ItemStack.
 *
 * This is a top-level interface. Except some generic use cases, you
 * probably will not directly work with this interface. Instead, you
 * will likely use the subclasses. Use your IDE to navigate them.
 */
interface NekoStack {

    /**
     * Gets the "wakame" [NBT][CompoundTag] on this item.
     */
    val nbt: CompoundTag

    /**
     * Gets the wrapped [ItemStack].
     *
     * The item stack may or may not be backed by a NMS object.
     *
     * ## When it is backed by a NMS object
     *
     * Any changes on `this` will reflect on the underlying game state, which
     * means: you may freely modify `this` and it will make sure that your
     * modifications will be directly and instantly applied to the world state.
     *
     * ## When it is backed by a strictly-Bukkit object
     *
     * Any changes on `this` will **NOT** apply to the underlying world state,
     * which means: you should only use `this` to add a new [ItemStack] to the
     * world state, such as giving it to players and dropping it on the ground.
     * In other words, if you have already added `this` to the world state, **DO
     * NOT** modify `this` and then expect that your changes will apply to the
     * world state.
     */
    val handle: ItemStack

    /**
     * The corresponding [NekoItem] this stack is generated from.
     */
    val prototype: NekoItem

    /**
     * The `namespace` of this item.
     *
     * The `namespace` is the name of the directory in which the item is defined in the config.
     */
    val namespace: String

    /**
     * The `path` of this item.
     *
     * The `path` is the name of the file in which the item is defined in the config.
     */
    val path: String

    /**
     * The namespaced identifier of this item.
     */
    val key: Key

    /**
     * The variant of this item.
     */
    var variant: Int

    /**
     * The UUID of this item.
     */
    val uuid: UUID

    /**
     * The inventory slot where this item becomes effective.
     */
    val slot: ItemSlot

    /**
     * 存放该物品的`物品组件`的容器. 该对象用于读取/添加/移除该物品的`物品组件`.
     */
    val components: ItemComponentMap

    /**
     * 存放该物品的`物品组件模板`的容器. 该对象用于读取物品模板的信息.
     */
    val templates: ItemTemplateMap

    /**
     * Removes all the custom tags from the item.
     *
     * **Only to be used in certain special cases!**
     */
    fun erase()

}

// fun main(nekoStack: NekoStack) {
//     // 射箭...
//     // 获取 arrow
//     val arrow: Arrow? = nekoStack.components.get(ItemComponentTypes.ARROW)
//     if (arrow != null && arrow.pierceLevel == 1.toByte()) {
//         nekoStack.components.add(ItemComponentTypes.ARROW, Arrow.Value(1))
//     }
//
//     val fireResistant: FireResistant? = nekoStack.components.get(ItemComponentTypes.FIRE_RESISTANT)
//     nekoStack.components.add(ItemComponentTypes.FIRE_RESISTANT, FireResistant)
//     nekoStack.components.remove(ItemComponentTypes.FIRE_RESISTANT)
//
//     val cells: ItemCells? = nekoStack.components.get(ItemComponentTypes.CELLS)
//     val newCells: ItemCells? = cells?.copy(a = 2)
//     newCells?.let { nekoStack.components.add(ItemComponentTypes.CELLS, it) }
//
//     // 渲染提示框
//     val attributable: Attributable? = nekoStack.components.get(ItemComponentTypes.ATTRIBUTABLE)
//     val attributableLoreLine: LoreLine? = nekoStack.components.render(ItemComponentTypes.ATTRIBUTABLE)
//     if (attributable != null) {
//         val loreLine: LoreLine = attributable.provideDisplayLore()
//     }
//
//     // 获取 item_name
//     val itemName: Component? = nekoStack.components.get(ItemComponentTypes.ITEM_NAME)
//     val unbreakable: Unbreakable? = nekoStack.components.get(ItemComponentTypes.UNBREAKABLE)
//     val hasFood: Boolean = nekoStack.components.has(ItemComponentTypes.FOOD)
//     // 设置 item_name
//     nekoStack.components.add(ItemComponentTypes.ITEM_NAME, Component.text("You can't change this name with anvils!"))
//     // 移除 cells (所有的词条栏)
//     nekoStack.components.remove(ItemComponentTypes.CELLS)
// }