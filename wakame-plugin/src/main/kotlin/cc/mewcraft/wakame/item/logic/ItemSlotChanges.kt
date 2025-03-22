package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.ItemSlotRegistry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.inventory.ItemStack

data class ItemSlotChanges(
    private val items: Reference2ObjectOpenHashMap<ItemSlot, Entry> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f),
) : Component<ItemSlotChanges> {
    companion object : ComponentType<ItemSlotChanges>()

    override fun type(): ComponentType<ItemSlotChanges> = ItemSlotChanges

    val changingItems: Collection<Entry>
        get() = items.values.filter { it.changing }

    operator fun get(itemSlot: ItemSlot): Entry {
        return items.computeIfAbsent(itemSlot) { Entry(itemSlot) }
    }

    /**
     * 记录对应的 [ItemSlot] 的变化.
     *
     * 一个 [Entry][cc.mewcraft.wakame.item.logic.ItemSlotChanges.Entry] 只会记录一个 [ItemSlot] 的变化.
     *
     * 如果 [changing] 为 `true`, 则表示该物品槽位在当前 tick 发生了变化, 反之则表示该物品槽位没有发生变化.
     * 设当前 tick 为 t, 当 [changing] 为 `true`: 则 [current] 为 t 刻的 [ItemStack],
     * [previous] 为 t-1 刻的 [ItemStack]. 当 [changing] 为 `false`: [previous] 与 [current] 是一致的,
     * 判断 [previous] 与 [current] 没有任何意义.
     */
    class Entry internal constructor(
        /**
         * 物品发生变化所在的 [ItemSlot].
         */
        val slot: ItemSlot,
    ) {

        /**
         * 发生变化后的物品.
         */
        var current: ItemStack? = null
            private set

        /**
         * 发生变化前的物品.
         */
        var previous: ItemStack? = null
            private set

        /**
         * [slot] 在当前 tick 是否发生了变化.
         */
        var changing: Boolean = false
            private set

        operator fun component1(): ItemSlot = slot
        operator fun component2(): ItemStack? = current
        operator fun component3(): ItemStack? = previous
        operator fun component4(): Boolean = changing

        internal fun update(current: ItemStack?) {
            require(current?.isEmpty != true) { "current cannot be empty" }

            this.current = current?.clone().also { this.previous = this.current }
            this.changing = this.current != this.previous
        }
    }
}