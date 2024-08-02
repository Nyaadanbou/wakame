package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

/**
 * A wrapper of an ItemStack, which provides dedicated properties and
 * functions to manipulate wakame data on the ItemStack.
 *
 * This is a top-level interface. Except some generic use cases, you
 * probably will not directly work with this interface. Instead, you
 * will likely use the subclasses. Use your IDE to navigate them.
 */
interface NekoStack : Keyed, Examinable {

    /**
     * Gets the "wakame" [NBT][CompoundTag] on this item.
     */
    @get:ApiStatus.Internal
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
    @get:ApiStatus.Internal
    val handle: ItemStack

    /**
     * Gets the clone of [handle].
     */
    val itemStack: ItemStack

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
    override val key: Key

    /**
     * The variant of this item.
     */
    var variant: Int

    /**
     * The inventory slot group where this item becomes effective.
     */
    val slotGroup: ItemSlotGroup // TODO implement it

    /**
     * The corresponding [NekoItem] this stack is generated from.
     */
    val prototype: NekoItem

    /**
     * 存放该物品的`物品组件`的容器. 该对象用于操作该物品的组件信息.
     */
    val components: ItemComponentMap

    /**
     * 存放该物品的`物品模板`的容器. 该对象用于读取物品模板的信息.
     */
    val templates: ItemTemplateMap

    /**
     * 存放物品的`物品行为`的容器. 该对象用于获取该物品的行为逻辑.
     */
    val behaviors: ItemBehaviorMap

    /**
     * 返回一个克隆.
     */
    fun clone(): NekoStack

    /**
     * Removes all the custom tags from the item.
     *
     * **Only to be used in certain special cases!**
     */
    @ApiStatus.Internal
    fun erase()

}
