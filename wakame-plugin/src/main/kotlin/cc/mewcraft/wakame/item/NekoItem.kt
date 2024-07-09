package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.behavior.ItemBehaviorMap
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.vanilla.VanillaComponentRemover
import cc.mewcraft.wakame.user.User
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/**
 * Represents an **item template**, or a "blueprint" in other words.
 * Essentially, this is an encapsulation of an item configuration.
 *
 * The design philosophy of `this` is, that you can use a [NekoItem] as
 * a **blueprint** to create as many [NekoStacks][NekoStack] as
 * you want by calling [NekoItemRealizer.realize], where each of the
 * ItemStack will have the data of different values, and even have the
 * data of different types. This allows us to create more possibilities
 * for items, achieving better game experience by randomizing the item
 * generation and hence reducing duplication.
 *
 * @see NekoStack
 */
interface NekoItem : Keyed, Examinable {
    /**
     * The [key][Key] of this item, where:
     * - [namespace][Key.namespace] is the name of the directory which contains the config file
     * - [value][Key.value] is the name of the config file itself (without the file extension)
     */
    override val key: Key

    /**
     * The UUID of this item.
     */
    val uuid: UUID

    /**
     * The [config provider][ConfigProvider] of this item.
     */
    val config: ConfigProvider

    /**
     * The [key][Key] to the ItemType of this item.
     */
    val itemType: Key

    /**
     * The inventory slot where this item can take effect.
     */
    val slot: ItemSlot

    /**
     * 需要移除的原版物品组件.
     */
    val removeComponents: VanillaComponentRemover

    /**
     * 物品组件的模板.
     */
    val templates: ItemTemplateMap

    // 开发日记 2024/6/25
    // 物品行为在配置文件中跟物品组件是同级的一类设置 (这两者在配置文件里没有区别).
    // 也就是说, 配置文件中的同一个节点, 可能既会被物品行为使用, 也会被物品组件使用.

    /**
     * The list of behaviors of this item.
     */
    val behaviors: ItemBehaviorMap
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
    val realizer: NekoItemRealizer by inject()
}
