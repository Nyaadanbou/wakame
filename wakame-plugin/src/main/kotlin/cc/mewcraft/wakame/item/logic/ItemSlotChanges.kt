package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.ItemSlotRegistry
import cc.mewcraft.wakame.util.item.isEmpty
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.inventory.ItemStack

data class ItemSlotChanges(
    /**
     * 在 [ItemSlotChangeEventInternals] 执行前, 保存了每个槽位 t - 1 刻的 [Entry].
     * 在 [ItemSlotChangeEventInternals] 执行后, 该映射会更新为当前刻的 [Entry].
     *
     * @see ItemSlotChangeEventInternals
     */
    private val items: Reference2ObjectOpenHashMap<ItemSlot, Entry> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f),
) : Component<ItemSlotChanges> {
    companion object : ComponentType<ItemSlotChanges>()

    override fun type(): ComponentType<ItemSlotChanges> = ItemSlotChanges

    val changingItems: Collection<Entry>
        get() = items.values.filter { it.isChanging }

    operator fun get(itemSlot: ItemSlot): Entry {
        return items.computeIfAbsent(itemSlot) { Entry(itemSlot, null, null, false) }
    }

    /**
     * ## 注意!
     *
     * 获取该类 [ItemStack] 相关变量时分两种情况:
     * 1. 如果此变量在 [ItemSlotChangeEventInternals] 执行前获取, 则返回 t - 1 刻的 [ItemStack].
     * 2. 如果此变量在 [ItemSlotChangeEventInternals] 执行后被获取, 则返回当前刻的 [ItemStack].
     *
     * 该变量不会是 [ItemStack.isEmpty] 为 `true` 的物品! 对于这类物品, 该变量会直接是 `null` 来表示它们.
     */
    class Entry(
        val slot: ItemSlot,
        var current: ItemStack?,
        var previous: ItemStack?,
        var isChanging: Boolean,
    ) {
        init {
            previous?.let { require(!it.isEmpty) { "previous cannot be empty" } }
            current?.let { require(!it.isEmpty) { "current cannot be empty" } }
        }

        operator fun component1(): ItemSlot = slot
        operator fun component2(): ItemStack? = current
        operator fun component3(): ItemStack? = previous

        fun update(newItem: ItemStack?) {
            newItem?.let { require(!it.isEmpty) { "newItem cannot be empty" } }
            val previous = current
            this.current = newItem?.clone()
            this.previous = previous
            isChanging = current != previous
        }
    }
}