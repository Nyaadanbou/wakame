package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.BaseBinaryKeys
import cc.mewcraft.wakame.item.binary.show.CustomDataAccessor
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingCustomName
import cc.mewcraft.wakame.util.backingLore
import cc.mewcraft.wakame.util.isNms
import cc.mewcraft.wakame.util.removeWakameTag
import cc.mewcraft.wakame.util.wakameTag
import cc.mewcraft.wakame.util.wakameTagOrNull
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

typealias PlayNekoStackPredicate = PlayNekoStack.() -> Boolean
typealias ShowNekoStackPredicate = ShowNekoStack.() -> Boolean

val ItemStack.isNeko: Boolean
    get() {
        val compound = wakameTagOrNull ?: return false
        val key = NekoStackImplementation.getKey(compound) ?: return false
        return ItemRegistry.INSTANCES.find(key) != null
    }

val ItemStack.tryNekoStack: PlayNekoStack?
    get() {
        if (!this.hasItemMeta()) return null
        if (!this.isNms) return null
        if (!this.isNeko) return null
        if (!BukkitNekoStackImplementation.isPlay(this.wakameTag)) return null
        return PlayNekoStackImpl(this)
    }

val ItemStack.toNekoStack: PlayNekoStack
    get() {
        require(this.hasItemMeta()) { "The ItemStack has no ItemMeta" }
        require(this.isNms) { "The ItemStack is not an NMS object" }
        require(this.isNeko) { "The itemStack is not from Wakame" }
        require(BukkitNekoStackImplementation.isPlay(this.wakameTag)) { "The ItemStack is not a play NekoStack" }
        return PlayNekoStackImpl(this)
    }

val ItemStack.tryShowNekoStack: ShowNekoStack?
    get() {
        if (!this.hasItemMeta()) return null
        if (!this.isNeko) return null
        return ShowNekoStackImpl(this.clone())
    }

val ItemStack.toShowNekoStack: ShowNekoStack
    get() {
        require(this.hasItemMeta()) { "The ItemStack has no ItemMeta" }
        require(this.isNeko) { "The itemStack is not from Wakame" }
        return ShowNekoStackImpl(this.clone())
    }

/**
 * This function is meant to be used to create a new [PlayNekoStack]
 * **from scratch** which will ultimately be added to the world state,
 * such as adding it to a player's inventory.
 *
 * ## Caution
 *
 * It is the caller's responsibility to modify the returned [PlayNekoStack]
 * before it's added to the world state so that it becomes a legal NekoItem.
 * Otherwise, undefined behaviors can occur.
 */
fun Material.createNekoStack(): PlayNekoStack {
    return PlayNekoStackImpl(this)
}

internal object BukkitNekoStackImplementation {
    fun isPlay(nekoCompound: CompoundShadowTag): Boolean {
        return !isShow(nekoCompound) // an NS is either PNS or SNS
    }

    fun isShow(nekoCompound: CompoundShadowTag): Boolean {
        return nekoCompound.contains(BaseBinaryKeys.SHOW)
    }
}

// Injected dependencies
private object BukkitNekoStackDependencies : KoinComponent {
    val LOGGER: Logger by inject()
}

// Common code shared by BukkitNekoStack implementations
private interface BukkitNekoStackBase : NekoStackBase, BukkitNekoStack {
    override fun erase() {
        itemStack.removeWakameTag()
    }
}

// PlayNekoStack impl
private class PlayNekoStackImpl(
    override val itemStack: ItemStack,
) : BukkitNekoStackBase, PlayNekoStack {
    constructor(mat: Material) : this(
        itemStack = ItemStack(mat), // strictly-Bukkit ItemStack
    )

    override val tags: CompoundShadowTag
        get() {
            if (!itemStack.isNms) {
                // If this is a strictly-Bukkit ItemStack,
                // the `wakame` compound should always be available (if not, create it)
                // as we need to create a NekoItem realization from an empty ItemStack.
                return itemStack.wakameTag
            }
            // If this is a NMS-backed ItemStack,
            // reading/modifying is allowed only if it already has a `wakame` compound.
            // We explicitly prohibit modifying the ItemStacks, which are not already
            // NekoItem realization, in the world state because we want to avoid
            // undefined behaviors. Just imagine that a random code modifies a
            // vanilla item and make it an incomplete realization of NekoItem.
            return itemStack.wakameTagOrNull ?: throw NullPointerException(
                "Can't read/modify the tags of NMS-backed ItemStack which is not NekoItem realization"
            )
        }

    override val show: ShowNekoStack
        get() {
            // Always make a copy
            val copy: ItemStack = this.itemStack.clone()
            val showStack = ShowNekoStackImpl(copy)
            ShowNekoStackImplementation.writeSNSMark(showStack.tags)
            return showStack
        }

    override val play: PlayNekoStack
        get() = this
}

// ShowNekoStack impl
private class ShowNekoStackImpl(
    override val itemStack: ItemStack,
) : BukkitNekoStackBase, ShowNekoStack {
    // The `wakame` compound can always be available (if not, create it)
    // as the ItemStack is solely used for the purpose of display, not for
    // the purpose of being used by players. Therefore, we can relax the
    // restrictions a little.
    override val tags: CompoundShadowTag
        get() = itemStack.wakameTag

    override val customData: CustomDataAccessor
        get() = TODO("Not yet implemented")

    override val show: ShowNekoStack
        get() = this

    override val play: PlayNekoStack
        get() {
            // Always make a copy
            val copy: ItemStack = this.itemStack.clone()

            // Remove custom name and lore as they are handled by the packet system
            copy.backingCustomName = null
            copy.backingLore = null
            copy.backingCustomModelData = null

            // Create a new PlayNekoStack wrapping the stack
            val playStack = PlayNekoStackImpl(copy)
            // Side note:
            // The stack should already be a legal neko item.
            // We don't need to check the legality here.

            // Remove SNS mark
            ShowNekoStackImplementation.removeSNSMark(playStack.tags)

            return playStack
        }
}

private object ShowNekoStackImplementation {
    fun writeSNSMark(compoundTag: CompoundShadowTag) {
        compoundTag.putByte(BaseBinaryKeys.SHOW, 0) // 写入 SNS mark，告知发包系统不要修改此物品
    }

    fun removeSNSMark(compoundTag: CompoundShadowTag) {
        compoundTag.remove(BaseBinaryKeys.SHOW) // 移除 SNS mark
    }
}
