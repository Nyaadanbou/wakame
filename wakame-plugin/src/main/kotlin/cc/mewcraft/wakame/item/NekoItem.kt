package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import org.bukkit.entity.Player

/**
 * Represents an **item template**, or an "archetype" in other words.
 * Essentially, this is an encapsulation of an item's configuration.
 *
 * The design philosophy of `this` is, that you can use a [NekoItem] as
 * an **archetype** to create as many [NekoStacks][NekoStack] as you want
 * by calling [NekoItemRealizer.realize], where each of the ItemStack
 * will have the data of different values, and even have the data of
 * different types. This allows us to create more possibilities for items,
 * achieving better game experience by randomizing the item generation
 * and hence reducing duplication.
 *
 * @see NekoStack
 */
interface NekoItem : Examinable {

    /**
     * 包含快速获取特殊 [NekoItem] 实例的函数.
     */
    companion object {
        /**
         * 获取空的 [NekoItem] 实例.
         */
        fun empty(): NekoItem {
            return EmptyNekoItem
        }
    }

    /**
     * The namespaced identifier of this item, where:
     * - *namespace* is the name of the directory which contains the config file
     * - *value* is the name of the config file itself (without the file extension)
     */
    val id: Key

    /**
     * 物品的基底.
     */
    val base: ItemBase

    /**
     * 物品的名字, 用于展示给玩家.
     */
    val name: Component

    /**
     * 物品的名字, 用于后台日志和所有不支持 [net.kyori.adventure.text.Component] 的地方.
     */
    val plainName: String

    /**
     * The item slot group where this item can take effect.
     */
    val slotGroup: ItemSlotGroup

    /**
     * 该物品是否隐藏.
     *
     * 隐藏起来的物品无法通过指令访问, 也不会出现在物品图鉴中, 但代码仍然可以通过注册表直接访问.
     */
    val hidden: Boolean

    /**
     * 物品组件的模板.
     */
    val templates: ItemTemplateMap

    /**
     * 物品的行为.
     */
    val behaviors: ItemBehaviorMap
}

/* Implementations */

fun NekoItem.realize(): NekoStack {
    return StandardNekoItemRealizer.realize(this)
}

fun NekoItem.realize(context: ItemGenerationContext): NekoStack {
    return StandardNekoItemRealizer.realize(this, context)
}

fun NekoItem.realize(player: Player): NekoStack {
    return StandardNekoItemRealizer.realize(this, player)
}

fun NekoItem.realize(crate: Crate): NekoStack {
    return StandardNekoItemRealizer.realize(this, crate)
}
