package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.ItemSlotRegistry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.inventory.ItemStack

data class ItemRecord(
    /**
     * 在 [ItemSlotChangeEventInternals] 执行前, 保存了每个槽位在 `t-1` 刻的物品.
     * 在 [ItemSlotChangeEventInternals] 执行后, 该映射会更新为当前刻的物品.
     *
     * ## 注意!
     *
     * 该映射不储存 [org.bukkit.inventory.ItemStack.isEmpty] 为 `true` 的物品. 对于这类物品, 该映射会直接将其储存为 `null`.
     *
     * @see ItemSlotChangeEventInternals
     */
    private val items: Reference2ObjectOpenHashMap<ItemSlot, ItemStack?> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f),
) : Component<ItemRecord> {
    companion object : ComponentType<ItemRecord>()

    override fun type(): ComponentType<ItemRecord> = ItemRecord

    /**
     * 分两种情况:
     * 1. 如果此函数在 [ItemSlotChangeEventInternals] 执行前被调用, 则返回 `t-1` 刻的物品.
     * 2. 如果此函数在 [ItemSlotChangeEventInternals] 执行后被调用, 则返回当前刻的物品.
     *
     * 该函数不会返回 [ItemStack.isEmpty] 为 `true` 的物品! 对于这类物品, 该函数会直接返回 `null` 来表示它们.
     */
    operator fun get(itemSlot: ItemSlot): ItemStack? {
        return items[itemSlot]
    }

    /**
     * 储存当前 tick 的物品.
     *
     * 你必须保证传入该函数的 [itemStack] 的 [ItemStack.isEmpty] 不返回 `true`. 对于这类物品, 你应该直接传入 `null`.
     */
    operator fun set(itemSlot: ItemSlot, itemStack: ItemStack?) {
        items.put(itemSlot, itemStack?.clone())
    }
}