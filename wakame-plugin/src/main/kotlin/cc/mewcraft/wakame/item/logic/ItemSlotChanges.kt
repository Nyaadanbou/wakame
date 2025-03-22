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
     */
    class Entry internal constructor(
        /**
         * 物品发生变化所在的 [ItemSlot].
         */
        val slot: ItemSlot,
    ) {

        /**
         * 发生变化后的[物品][ItemStack].
         *
         * 如果为空, 则表示该物品为不存在或为空气.
         *
         * ## 注意!
         *
         * 如果在 [ItemSlotChangeEventInternals] 进行 tick 之前获取此物品, 将会返回上一 tick 的物品结果.
         */
        var current: ItemStack? = null
            private set

        /**
         * 发生变化前的[物品][ItemStack].
         *
         * 如果为空, 则表示该物品为不存在或为空气.
         *
         * ## 注意!
         *
         * 如果在 [ItemSlotChangeEventInternals] 进行 tick 之前获取此物品, 将会返回上一 tick 的物品结果.
         */
        var previous: ItemStack? = null
            private set

        /**
         * 记录是否发生了变化.
         *
         * 如果为 `true`, 则表示该物品发生了变化, 反之则表示该物品没有发生变化.
         */
        var changing: Boolean = false
            private set

        operator fun component1(): ItemSlot = slot
        operator fun component2(): ItemStack? = current
        operator fun component3(): ItemStack? = previous
        operator fun component4(): Boolean = changing

        fun update(newItem: ItemStack?) {
            if (newItem != null) {
                if (newItem.isEmpty)
                    error("newItem cannot be empty")
            }

            this.current = newItem?.clone().also { this.previous = this.current }
            this.changing = this.current != this.previous
        }
    }
}