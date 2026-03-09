package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.item.property.impl.ItemSlotRegistry
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.ObjectIterators
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// ------------
// 监测玩家背包中物品的变化
// ------------

/**
 * 该类属于事件 [cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent] 内部完整实现的一部分.
 *
 * 该类扫描玩家背包的变化, 按情况触发 [cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent].
 *
 * 具体来说:
 * 我们每 tick 扫描每个玩家背包里的特定槽位上的物品.
 * 如果第 `n` tick 扫描的结果和第 `n-1` tick 扫描的结果不同,
 * 则认为这个槽位发生了变化, 那么此时就会记录并触发一个事件.
 */
object ScanItemSlotChanges : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) {
        for (slot in ItemSlotRegistry.itemSlots()) {
            val curr = slot.getItem(player)
            val itemSlotChanges = user.itemSlotChanges
            val entry = itemSlotChanges[slot]
            entry.update(curr)
            if (entry.changing) {
                val prev = entry.previous
                val curr = entry.current
                PlayerItemSlotChangeEvent(player, slot, prev, curr).callEvent()
            }
        }
    }
}

interface ItemSlotChanges {
    companion object {
        fun empty(): ItemSlotChanges = EmptyItemSlotChanges
        fun create(): ItemSlotChanges = MutableItemSlotChanges()
        fun create(player: Player): ItemSlotChanges = create()
    }

    fun fastIterator(): ObjectIterator<Reference2ObjectMap.Entry<ItemSlot, Entry>>

    operator fun get(itemSlot: ItemSlot): Entry

    /**
     * 记录对应的 [ItemSlot] 的变化.
     *
     * 一个 [Entry] 只会记录一个 [ItemSlot] 的变化.
     *
     * 如果 [changing] 为 `true`, 则表示该物品槽位在当前 tick 发生了变化, 反之则表示该物品槽位没有发生变化.
     *
     * 设当前 tick 为 t:
     *
     * 当 [changing] 为 `true` 时, [current] 为 t 刻的 [ItemStack], [previous] 为 t-1 刻的 [ItemStack].
     * 当 [changing] 为 `false` 时, [previous] 与 [current] 相同, 判断 [previous] 与 [current] 没有任何意义.
     */
    class Entry internal constructor(
        /**
         * 是否冻结. 冻结状态下 [update] 为 no-op, [changing] 永远为 `false`.
         */
        val frozen: Boolean = false,
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

        operator fun component1(): ItemStack? = current
        operator fun component2(): ItemStack? = previous

        internal fun update(current: ItemStack?) {
            if (frozen) return
            if (current == null && this.current == null && this.previous == null) {
                return // 都为空则可以直接返回, 不执行写操作以提升性能
            }
            if (current == this.current) {
                this.changing = false
                return
            }
            this.previous = this.current
            this.current = current?.clone()
            this.changing = true
        }
    }
}

/**
 * 遍历所有在当前 tick 发生了变化的物品槽位.
 */
inline fun ItemSlotChanges.forEachChangingEntry(
    action: (slot: ItemSlot, curr: ItemStack?, prev: ItemStack?) -> Unit,
) {
    for ((slot, entry) in fastIterator()) {
        if (entry.changing) {
            action(slot, entry.current, entry.previous)
        }
    }
}

private data object EmptyItemSlotChanges : ItemSlotChanges {
    private val NO_OP_ENTRY = ItemSlotChanges.Entry(frozen = true)

    override fun fastIterator(): ObjectIterator<Reference2ObjectMap.Entry<ItemSlot, ItemSlotChanges.Entry>> {
        return ObjectIterators.emptyIterator()
    }

    override fun get(itemSlot: ItemSlot): ItemSlotChanges.Entry {
        return NO_OP_ENTRY
    }
}

private data class MutableItemSlotChanges(
    private val entries: Reference2ObjectOpenHashMap<ItemSlot, ItemSlotChanges.Entry> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f),
) : ItemSlotChanges {

    override fun fastIterator(): ObjectIterator<Reference2ObjectMap.Entry<ItemSlot, ItemSlotChanges.Entry>> {
        return entries.reference2ObjectEntrySet().fastIterator()
    }

    override operator fun get(itemSlot: ItemSlot): ItemSlotChanges.Entry {
        return entries.computeIfAbsent(itemSlot) { _ -> ItemSlotChanges.Entry() }
    }
}