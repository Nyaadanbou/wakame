package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.BaseBinaryKeys
import cc.mewcraft.wakame.item.binary.show.CustomDataAccessor
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingCustomName
import cc.mewcraft.wakame.util.backingLore
import cc.mewcraft.wakame.util.isNmsObjectBacked
import cc.mewcraft.wakame.util.nekoCompound
import cc.mewcraft.wakame.util.nekoCompoundOrNull
import cc.mewcraft.wakame.util.removeNekoCompound
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * This type alias is used to verify whether a PlayNekoStack should be considered
 * "effective" for the player. By "effective", we mean for example:
 * - whether the item is in an effective slot, or
 * - whether the item has certain behavior enabled
 * - etc.
 */
typealias PlayNekoStackPredicate = PlayNekoStack.() -> Boolean

/**
 * The same as [PlayNekoStackPredicate] but for [ShowNekoStack].
 */
typealias ShowNekoStackPredicate = ShowNekoStack.() -> Boolean

/**
 * Wraps the [ItemStack] as a [PlayNekoStack].
 */
val ItemStack.playNekoStackOrNull: PlayNekoStack?
    get() = PlayNekoStackFactory.maybe(this)

/**
 * Wraps the [ItemStack] as a [PlayNekoStack].
 *
 * @throws IllegalArgumentException
 */
val ItemStack.playNekoStack: PlayNekoStack
    get() = PlayNekoStackFactory.require(this)

/**
 * Wraps the [ItemStack] as a [ShowNekoStack].
 */
val ItemStack.showNekoStackOrNull: ShowNekoStack?
    get() = ShowNekoStackFactory.maybe(this)

/**
 * Wraps the [ItemStack] as a [ShowNekoStack].
 *
 * @throws IllegalArgumentException
 */
val ItemStack.showNekoStack: ShowNekoStack
    get() = ShowNekoStackFactory.require(this)

/**
 * The factory of [PlayNekoStack].
 */
object PlayNekoStackFactory {
    /**
     * Wraps the [itemStack] as a [PlayNekoStack] object. Then, you can use
     * it to directly read/modify the wrapped [itemStack] in the world state.
     *
     * This function requires the [itemStack] to fulfill all the requirements:
     * 1. The [itemStack] is backed by an NMS object
     * 2. The [itemStack] is a NekoItem realization
     * 3. The [itemStack] is a legal PlayNekoStack
     *
     * If any of the requirements are not fulfilled, this function will return `null`.
     *
     * @throws IllegalArgumentException if the [itemStack] instance is not
     *     backed by an NMS object
     */
    fun maybe(itemStack: ItemStack): PlayNekoStack? {
        if (!itemStack.hasItemMeta()) return null // Optimization - fast return
        require(itemStack.isNmsObjectBacked) { "The ItemStack is not backed by an NMS object" }
        val playNekoStack = PlayNekoStackImpl(itemStack).takeIf { it.isNeko && it.isPlay }
        return playNekoStack
    }

    /**
     * Wraps the [itemStack] as a [PlayNekoStack] object. Then, you can use
     * it to directly read/modify the wrapped [itemStack] in the world state.
     *
     * This function requires the [itemStack] to fulfill all the requirements:
     * 1. The [itemStack] is backed by an NMS object
     * 2. The [itemStack] is a NekoItem realization
     * 3. The [itemStack] is a legal PlayNekoStack
     *
     * If any of the requirements are not fulfilled, this function will throw [IllegalArgumentException].
     *
     * @throws IllegalArgumentException if the [itemStack] does not fulfill
     * the requirements
     */
    fun require(itemStack: ItemStack): PlayNekoStack {
        require(itemStack.hasItemMeta()) { "The ItemStack has no ItemMeta" } // Optimization - fast fail
        require(itemStack.isNmsObjectBacked) { "The ItemStack is not backed by an NMS object" }
        val playNekoStack = PlayNekoStackImpl(itemStack)
        require(playNekoStack.isNeko) { "The ItemStack is not a legal NekoItem" }
        require(playNekoStack.isPlay) { "The ItemStack is not a legal PlayNekoStack" }
        return playNekoStack
    }

    /**
     * This function is meant to be used to create a new [PlayNekoStack]
     * which will ultimately be added to the world state (such as adding
     * it to a player's inventory and dropping it on the ground).
     *
     * ## Caution
     *
     * It is the caller's responsibility to modify the returned [PlayNekoStack]
     * before it's added to the world state so that it becomes a legal realization
     * of NekoItem. Otherwise, undefined behaviors can occur.
     */
    fun new(material: Material): PlayNekoStack {
        return PlayNekoStackImpl(material)
    }
}

/**
 * The factory of [PlayNekoStack].
 */
object ShowNekoStackFactory {
    /**
     * Wraps the [itemStack] as a [ShowNekoStack] object.
     *
     * Returns `null` if the [itemStack] is not already of legal [ShowNekoStack].
     *
     * **The given [itemStack] will leave intact.**
     */
    fun maybe(itemStack: ItemStack): ShowNekoStack? {
        val showNekoStack = ShowNekoStackImpl(itemStack.clone()).takeIf { it.isNeko && it.isShow }
        return showNekoStack
    }

    /**
     * Wraps the [itemStack] as a [ShowNekoStack] object.
     *
     * Throws an exception if the [itemStack] is not already of legal [ShowNekoStack].
     *
     * **The given [itemStack] will leave intact.**
     *
     * @throws IllegalArgumentException
     */
    fun require(itemStack: ItemStack): ShowNekoStack {
        val showNekoStack = ShowNekoStackImpl(itemStack.clone())
        require(showNekoStack.isNeko) { "The ItemStack is not a legal NekoItem" }
        require(showNekoStack.isShow) { "The ItemStack is not a legal ShowNekoStack" }
        return showNekoStack
    }
}

private interface BukkitNekoStackBase : NekoStackBase, BukkitNekoStack {
    override val isNmsBacked: Boolean
        get() = itemStack.isNmsObjectBacked

    override val isNeko: Boolean
        get() = itemStack.nekoCompoundOrNull != null

    override val isPlay: Boolean
        get() = !isShow // an NS is either PNS or SNS

    override val isShow: Boolean
        get() = tags.contains(BaseBinaryKeys.SHOW, ShadowTagType.BYTE)

    override fun erase() {
        itemStack.removeNekoCompound()
    }
}

private class PlayNekoStackImpl(
    override val itemStack: ItemStack,
) : BukkitNekoStackBase, PlayNekoStack {
    constructor(mat: Material) : this(
        itemStack = ItemStack(mat), // strictly-Bukkit ItemStack
    )

    override val tags: CompoundShadowTag
        get() {
            if (!isNmsBacked) {
                // If this is a strictly-Bukkit ItemStack,
                // the `wakame` compound should always be available (if not, create it)
                // as we need to create a NekoItem realization from an empty ItemStack.
                return itemStack.nekoCompound
            }
            // If this is a NMS-backed ItemStack,
            // reading/modifying is allowed only if it already has a `wakame` compound.
            // We explicitly prohibit modifying the ItemStacks, which are not already
            // NekoItem realization, in the world state because we want to avoid
            // undefined behaviors. Just imagine that a random code modifies a
            // vanilla item and make it an incomplete realization of NekoItem.
            return itemStack.nekoCompoundOrNull ?: throw NullPointerException(
                "Can't read/modify the tags of NMS-backed ItemStack which is not NekoItem realization"
            )
        }

    override val show: ShowNekoStack
        get() {
            // Always make a copy
            val stackCopy = this.itemStack.clone()
            val showStack = ShowNekoStackImpl(stackCopy)
            showStack.tags.writeSNSMark()
            return showStack
        }

    override val play: PlayNekoStack
        get() = this
}

private class ShowNekoStackImpl(
    override val itemStack: ItemStack,
) : BukkitNekoStackBase, ShowNekoStack {
    // The `wakame` compound can always be available (if not, create it)
    // as the ItemStack is solely used for the purpose of display, not for
    // the purpose of being used by players. Therefore, we can relax the
    // restrictions a little.
    override val tags: CompoundShadowTag
        get() = itemStack.nekoCompound

    override val customData: CustomDataAccessor
        get() = TODO("Not yet implemented")

    override val show: ShowNekoStack
        get() = this

    override val play: PlayNekoStack
        get() {
            // Always make a copy
            val stackCopy = this.itemStack.clone()

            // Remove custom name and lore as they are handled by the packet system
            stackCopy.backingCustomName = null
            stackCopy.backingLore = null
            stackCopy.backingCustomModelData = null

            // Create a new PlayNekoStack wrapping the stack
            val playStack = PlayNekoStackImpl(stackCopy)
            // Side note:
            // The stack should already be a legal neko item.
            // We don't need to check the legality here.

            // Remove SNS mark
            playStack.tags.removeSNSMark()

            return playStack
        }
}

private fun CompoundShadowTag.writeSNSMark() {
    putByte(BaseBinaryKeys.SHOW, 0) // 写入 SNS mark，告知发包系统不要修改此物品
}

private fun CompoundShadowTag.removeSNSMark() {
    remove(BaseBinaryKeys.SHOW) // 移除 SNS mark
}
