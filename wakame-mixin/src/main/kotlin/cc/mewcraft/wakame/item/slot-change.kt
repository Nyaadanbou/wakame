package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.entity.player.component.InventoryListenable
import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.item.property.impl.ItemSlotRegistry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import it.unimi.dsi.fastutil.objects.ObjectIterator
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus

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
object ScanItemSlotChanges : IteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, InventoryListenable) }
), FamilyOnAdd {
    override fun onTickEntity(entity: Entity) {
        val player = entity[BukkitPlayer].unwrap()
        for (slot in ItemSlotRegistry.itemSlots()) {
            val curr = slot.getItem(player)
            val slotChanges = entity[ItemSlotChanges]
            val entry = slotChanges[slot]
            entry.update(curr)
            if (entry.changing) {
                val prev = entry.previous
                val curr = entry.current
                if (!PlayerItemSlotChangeEvent.getHandlerList().registeredListeners.isEmpty()) {
                    PlayerItemSlotChangeEvent(player, slot, prev, curr).callEvent()
                }
            }
        }
    }

    override fun onAddEntity(entity: Entity) {
        entity.configure { it += ItemSlotChanges() }
    }
}

data class ItemSlotChanges(
    @ApiStatus.Internal
    @JvmField
    val entries: Reference2ObjectOpenHashMap<ItemSlot, Entry> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f),
) : Component<ItemSlotChanges> {

    companion object : EComponentType<ItemSlotChanges>() {

        @Deprecated("use the new function", replaceWith = ReplaceWith("ItemStackEffectiveness.testSlot(slot, itemstack)"))
        fun testSlot(slot: ItemSlot, itemstack: ItemStack?): Boolean {
            return ItemStackEffectiveness.testSlot(slot, itemstack)
        }

        @Deprecated("use the new function", replaceWith = ReplaceWith("ItemStackEffectiveness.testLevel(player, itemstack)"))
        fun testLevel(player: Player, itemstack: ItemStack?): Boolean {
            return ItemStackEffectiveness.testLevel(player, itemstack)
        }

        @Deprecated("use the new function", replaceWith = ReplaceWith("ItemStackEffectiveness.testDamaged(itemstack)"))
        fun testDurability(itemstack: ItemStack): Boolean {
            return ItemStackEffectiveness.testDamaged(itemstack)
        }
    }

    override fun type(): EComponentType<ItemSlotChanges> = ItemSlotChanges

    @Deprecated("使用 forEachChangingEntry 以避免 Array 的创建")
    val changingEntries: Collection<Entry>
        get() = entries.values.filter(Entry::changing)

    fun fastIterator(): ObjectIterator<Reference2ObjectMap.Entry<ItemSlot, Entry>> {
        return entries.reference2ObjectEntrySet().fastIterator()
    }

    /**
     * 遍历所有在当前 tick 发生了变化的物品槽位.
     */
    inline fun forEachChangingEntry(
        action: (slot: ItemSlot, curr: ItemStack?, prev: ItemStack?) -> Unit,
    ) {
        for ((slot, entry) in fastIterator()) {
            if (entry.changing) {
                action(slot, entry.current, entry.previous)
            }
        }
    }

    operator fun get(itemSlot: ItemSlot): Entry {
        return entries.computeIfAbsent(itemSlot, ::Entry)
    }

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

        internal fun update(current: ItemStack?) {
            require(current?.isEmpty != true) { "current cannot be empty" }

            if (current == null && this.current == null && this.previous == null) {
                return // 都为空则可以直接返回, 不执行写操作以提升性能
            }

            this.current = current?.clone().also { this.previous = this.current }
            this.changing = this.current != this.previous
        }
    }
}