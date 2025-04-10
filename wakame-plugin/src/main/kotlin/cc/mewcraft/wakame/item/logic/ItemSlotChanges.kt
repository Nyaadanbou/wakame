package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.entity.player.koishLevel
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.ItemSlotRegistry
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.util.item.damage
import cc.mewcraft.wakame.util.item.isDamageable
import cc.mewcraft.wakame.util.item.maxDamage
import com.github.quillraven.fleks.Component
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class ItemSlotChanges(
    private val items: Reference2ObjectOpenHashMap<ItemSlot, Entry> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f),
) : Component<ItemSlotChanges> {

    companion object : EComponentType<ItemSlotChanges>() {
        /**
         * 检查物品在“正确”的物品槽.
         */
        fun testSlot(slot: ItemSlot, nekoStack: NekoStack?): Boolean {
            if (nekoStack == null) return false
            return nekoStack.slotGroup.contains(slot)
        }

        /**
         * 检查物品的等级小于等于玩家的冒险等级.
         */
        fun testLevel(player: Player, nekoStack: NekoStack?): Boolean {
            if (nekoStack == null) {
                return true // 如果不是萌芽物品, 那么玩家的等级一定高于该物品 (0)
            }

            val itemLevel = nekoStack.components.get(ItemComponentTypes.LEVEL)?.level
            if (itemLevel == null) {
                return true // 如果物品没有等级, 那么玩家的等级一定高于该物品 (0)
            }

            val playerLevel = player.koishLevel
            return itemLevel <= playerLevel
        }

        /**
         * 检查物品没有损坏.
         */
        fun testDurability(itemStack: ItemStack): Boolean {
            if (!itemStack.isDamageable) {
                return true // 如果物品有“无法破坏”或耐久组件不完整, 那么认为物品没有耐久度, 应该返回 true
            }

            if (itemStack.damage >= itemStack.maxDamage) {
                return false // 如果物品已经损坏, 那么应该返回 false
            }

            return true
        }
    }

    override fun type(): EComponentType<ItemSlotChanges> = ItemSlotChanges

    val changingItems: Collection<Entry>
        get() = items.values.filter(Entry::changing)

    operator fun get(itemSlot: ItemSlot): Entry {
        return items.computeIfAbsent(itemSlot, ::Entry)
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
        operator fun component4(): Boolean = changing

        internal fun update(current: ItemStack?) {
            require(current?.isEmpty != true) { "current cannot be empty" }

            this.current = current?.clone().also { this.previous = this.current }
            this.changing = this.current != this.previous
        }
    }
}