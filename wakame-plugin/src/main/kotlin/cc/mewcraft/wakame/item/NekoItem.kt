package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.user.User
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Represents an **item template**, or a "blueprint" in other words.
 * Essentially, this is an encapsulation of an item's configuration.
 *
 * The design philosophy of `this` is, that you can use a [NekoItem] as
 * a **blueprint** to create as many [NekoStacks][NekoStack] as you want
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
     * The item slot group where this item can take effect.
     */
    val slotGroup: ItemSlotGroup

    /**
     * 物品组件的模板.
     */
    val templates: ItemTemplateMap

    /**
     * 物品的行为.
     */
    val behaviors: ItemBehaviorMap
}

fun NekoItem.realize(): NekoStack {
    return NekoItemInjections.realizer.realize(this)
}

fun NekoItem.realize(context: GenerationContext): NekoStack {
    return NekoItemInjections.realizer.realize(this, context)
}

fun NekoItem.realize(user: User<*>): NekoStack {
    return NekoItemInjections.realizer.realize(this, user)
}

fun NekoItem.realize(crate: Crate): NekoStack {
    return NekoItemInjections.realizer.realize(this, crate)
}

private object NekoItemInjections : KoinComponent {
    val realizer: CustomNekoItemRealizer by inject()
}
