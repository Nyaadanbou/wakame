package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.util.isNmsObjectBacked
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

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
        require(itemStack.isNmsObjectBacked) { "The ItemStack is not backed by an NMS object" }
        val playNekoStack = PlayNekoStackImpl(itemStack)
        require(playNekoStack.isNeko) { "The ItemStack is not a NekoItem realization" }
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
     * This function will return `null` if the [itemStack] is not of a
     * legal [ShowNekoStack].
     *
     * The [itemStack] will leave intact.
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
     * This function will throw an exception if the [itemStack] is not of a
     * legal [ShowNekoStack].
     *
     * The [itemStack] will leave intact.
     *
     * @throws IllegalArgumentException
     */
    @Contract(pure = true)
    fun require(itemStack: ItemStack): ShowNekoStack {
        val itemStackCopy = itemStack.clone()
        val showNekoStack = ShowNekoStackImpl(itemStackCopy)
        require(showNekoStack.isNeko) { "The ItemStack is not a NekoItem realization" }
        require(showNekoStack.isShow) { "The ItemStack is not a legal ShowNekoStack" }
        return showNekoStack
    }

    /**
     * Converts the [playStack] to a [ShowNekoStack].
     *
     * The [playStack] will leave intact.
     */
    @Contract(pure = true) // 标记该函数不会修改传进来的 playStack
    fun convert(playStack: PlayNekoStack): ShowNekoStack {
        val itemStackCopy = playStack.itemStack.clone()
        val showNekoStack = ShowNekoStackImpl(itemStackCopy)
        showNekoStack.tags.writeSNSMark()
        return showNekoStack
    }

    /**
     * Converts the [itemStack] to a [ShowNekoStack].
     *
     * The [itemStack] will leave intact.
     *
     * @throws IllegalArgumentException if the [itemStack] is not a NekoItem realization
     */
    @Contract(pure = true)
    fun convert(itemStack: ItemStack): ShowNekoStack {
        val itemStackCopy = itemStack.clone()
        val showNekoStack = ShowNekoStackImpl(itemStackCopy)
        require(showNekoStack.isNeko) { "The ItemStack is not a NekoItem realization" }
        showNekoStack.tags.writeSNSMark()
        return showNekoStack
    }

    private fun CompoundShadowTag.writeSNSMark() {
        putByte(NekoTags.Root.SHOW, 0) // 写入 SNS mark，告知发包系统不要修改此物品
    }
}