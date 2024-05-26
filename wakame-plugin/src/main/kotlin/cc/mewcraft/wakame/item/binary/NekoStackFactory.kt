package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.packet.PacketNekoStackImpl
import cc.mewcraft.wakame.util.isNmsObjectBacked
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract
import com.github.retrooper.packetevents.protocol.item.ItemStack as PacketStack

val ItemStack.playNekoStackOrNull: PlayNekoStack?
    get() = PlayNekoStackFactory.maybe(this)

val ItemStack.playNekoStack: PlayNekoStack
    get() = PlayNekoStackFactory.require(this)

val ItemStack.showNekoStackOrNull: ShowNekoStack?
    get() = ShowNekoStackFactory.maybe(this)

val ItemStack.showNekoStack: ShowNekoStack
    get() = ShowNekoStackFactory.require(this)

val PacketStack.packetNekoStackOrNull: PacketNekoStack?
    get() = PacketNekoStackFactory.maybe(this)

val PacketStack.packetNekoStack: PacketNekoStack
    get() = PacketNekoStackFactory.require(this)

object PlayNekoStackFactory {
    /**
     * Wraps the [itemStack] as a [PlayNekoStack] object. Then, you can use
     * it to directly read/modify the wrapped [itemStack] in the world state.
     *
     * This function requires the [itemStack] to fulfill all the requirements:
     * 1. The [itemStack] is backed by an NMS object;
     * 2. The [itemStack] is a NekoItem realization;
     * 3. The [itemStack] is of legal PlayNekoStack.
     *
     * If any of the requirements are not fulfilled, this function will simply
     * return a `null`.
     *
     * @throws IllegalArgumentException if the [itemStack] instance is not
     *     backed by an NMS object
     */
    @Contract(pure = true)
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
     * 1. The [itemStack] is backed by an NMS object;
     * 2. The [itemStack] is a NekoItem realization;
     * 3. The [itemStack] is of legal PlayNekoStack.
     *
     * If any of the requirements are not fulfilled, this function will throw
     * an [IllegalArgumentException].
     *
     * @throws IllegalArgumentException if the [itemStack] does not fulfill
     * the requirements
     */
    @Contract(pure = true)
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
     * ## Caution!!!
     *
     * 1. It is the caller's responsibility to modify the returned [PlayNekoStack]
     * so that it becomes a legal realization of NekoItem. Fail to fulfill the
     * requirement will result in undefined behaviors.
     * 2. Once the returned [PlayNekoStack] has been added to the world state,
     * any subsequent changes to it **will not** reflect on that [PlayNekoStack]
     * in the world state. See [NekoStack.isNmsBacked] for the reasons.
     *
     * ## Tips
     *
     * If you want to modify the [PlayNekoStack]s that are already in the world
     * state (such as modifying the item in a player's inventory), use the
     * functions [maybe] or [require] to get a new wrapper object.
     */
    fun new(material: Material): PlayNekoStack {
        return PlayNekoStackImpl(material)
    }
}

object ShowNekoStackFactory {
    /**
     * Wraps the [itemStack] as a [ShowNekoStack] object.
     *
     * This function will return `null` if the [itemStack] is not already
     * of legal [ShowNekoStack].
     *
     * **The given [itemStack] will leave intact.**
     */
    @Contract(pure = true)
    fun maybe(itemStack: ItemStack): ShowNekoStack? {
        val itemStackCopy = itemStack.clone()
        val showNekoStack = ShowNekoStackImpl(itemStackCopy).takeIf { it.isNeko && it.isShow }
        return showNekoStack
    }

    /**
     * Wraps the [itemStack] as a [ShowNekoStack] object.
     *
     * This function will throw an exception if the [itemStack] is not already
     * of legal [ShowNekoStack].
     *
     * **The given [itemStack] will leave intact.**
     *
     * @throws IllegalArgumentException
     */
    @Contract(pure = true)
    fun require(itemStack: ItemStack): ShowNekoStack {
        val itemStackCopy = itemStack.clone()
        val showNekoStack = ShowNekoStackImpl(itemStackCopy)
        require(showNekoStack.isNeko) { "The ItemStack is not a legal NekoItem" }
        require(showNekoStack.isShow) { "The ItemStack is not a legal ShowNekoStack" }
        return showNekoStack
    }
}

object PacketNekoStackFactory {
    /**
     * Wraps the [itemStack] as a [PacketNekoStack] object.
     *
     * This function will return `null` if the [itemStack] is not already
     * of legal [PacketNekoStack].
     *
     * **The given [itemStack] will leave intact.**
     */
    @Contract(pure = true)
    fun maybe(itemStack: PacketStack): PacketNekoStack? {
        val itemStackCopy = itemStack.copy()
        val packetNekoStack = PacketNekoStackImpl(itemStackCopy).takeIf { it.isNeko }
        return packetNekoStack
    }

    /**
     * Wraps the [itemStack] as a [PacketNekoStack] object.
     *
     * This function will throw an exception if the [itemStack] is not already
     * of legal [PacketNekoStack].
     *
     * **The given [itemStack] will leave intact.**
     *
     * @throws IllegalArgumentException
     */
    @Contract(pure = true)
    fun require(itemStack: PacketStack): PacketNekoStack {
        val itemStackCopy = itemStack.copy()
        val packetNekoStack = PacketNekoStackImpl(itemStackCopy)
        require(packetNekoStack.isNeko) { "The ItemStack is not a legal NekoItem" }
        return packetNekoStack
    }
}