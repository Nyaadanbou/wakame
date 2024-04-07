package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.util.isNmsObjectBacked
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

/**
 * The factory used to create various [NekoStacks][NekoStack].
 */
object NekoStackFactory {
    /**
     * Gets the factory for [PlayNekoStack].
     */
    val PLAY = PlayNekoStackFactory

    /**
     * Gets the factory for [ShowNekoStack].
     */
    val SHOW = ShowNekoStackFactory
}

object PlayNekoStackFactory {
    /**
     * The same as [wrap] but it will return `null` if the [itemStack] is not
     * a legal neko item (that is, [NekoStack.isNeko] returns `false`).
     *
     * @throws IllegalArgumentException if the [itemStack] instance is not
     *     backed by an NMS object
     */
    @Contract(pure = false)
    fun by(itemStack: ItemStack): PlayNekoStack? {
        return wrap(itemStack).takeIf { it.isPlayStack }
    }

    /**
     * Wraps the [itemStack] as a [PlayNekoStack] object. Then, you can use
     * it to directly read/modify the wrapped [itemStack] in the world state
     * at your will.
     *
     * Note that the returned [PlayNekoStack] is not guaranteed to have the
     * [NekoStack.isNeko] be `true`. You should check the flag before you
     * further access the wakame data on the [itemStack].
     *
     * @throws IllegalArgumentException if the [itemStack] instance is not
     *     backed by an NMS object
     */
    @Contract(pure = false)
    fun wrap(itemStack: ItemStack): PlayNekoStack {
        require(itemStack.isNmsObjectBacked) { "Can't wrap a non NMS-backed ItemStack as NekoStack" }
        return PlayNekoStackImpl(itemStack)
    }

    /**
     * This function is meant to be used to create a new [PlayNekoStack]
     * which will ultimately be added to the world state (such as adding
     * it to a player's inventory and dropping it on the ground). Once
     * the [PlayNekoStack] has been added to the world state, any changes
     * to it **will not** reflect on that one in the world state.
     *
     * If you want to modify the [PlayNekoStack]s that are already in the world
     * state (such as modifying the item in a player's inventory), use the
     * functions [by] or [wrap] to get a new wrapper object.
     */
    fun new(material: Material): PlayNekoStack {
        return PlayNekoStackImpl(material)
    }
}

object ShowNekoStackFactory {
    // 尝试把一个 ItemStack 封装成 ShowNekoStack
    // 如果这个 ItemStack 原本不是 ShowNekoStack，那么直接返回 null
    // 使用场景：修改 GUI Inventory 里的 ItemStack
    @Contract(pure = false)
    fun by(itemStack: ItemStack): ShowNekoStack? {
        TODO()
    }

    // 把一个已经是 ShowNekoStack 的 ItemStack 封装成 ShowNekoStack
    // 如果这个 ItemStack 不是 ShowNekoStack，将抛出异常
    // 使用场景：修改 GUI Inventory 里的 ItemStack
    @Contract(pure = false)
    fun wrap(itemStack: ItemStack): ShowNekoStack {
        TODO()
    }

    // 把一个 PlayNekoStack 转换成 ShowNekoStack
    // 实际上只是写入一个特定的 NBT，标记其为 ShowNekoStack
    // 该标记直接影响一个物品的name/lore是否会被发包系统修改
    // 我们不想一个SNS被发包系统修改name/lore，对吧？
    @Contract(pure = true) // 标记该函数不会修改传进来的 playStack
    fun convert(playStack: PlayNekoStack): ShowNekoStack {
        TODO()
        // 在 wakame 下写入 byte('show'): 0b
    }
}