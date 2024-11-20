@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup

/**
 * 储存当前所有已加载的 [ItemSlot] 实例.
 *
 * [ItemSlot] 的实例在设计上是*按需创建*的. 每当序列化一个 [ItemSlot] 时,
 * 会自动注册到这个注册表中. 也就是说, 如果一个 [ItemSlot] 从未被序列化过,
 * 那么它就不会被注册到这个注册表中. 这么做是为了优化遍历性能.
 */
interface ItemSlotRegistry  {
    /**
     * 当前可用的 [ItemSlot] 的实例数.
     */
    val size: Int

    /**
     * 获取当前所有已经注册的 [ItemSlot].
     */
    fun all(): Set<ItemSlot>

    /**
     * 获取自定义的 [ItemSlot] 实例.
     * 这些实例代表是非原版的装备栏位, 例如非双手/非盔甲.¬
     */
    fun custom(): Set<ItemSlot>

    /**
     * 获取一个 [EquipmentSlotGroup] 所对应的 [ItemSlot].
     * 如果不存在, 则返回一个空集合.
     */
    fun get(group: EquipmentSlotGroup): Set<ItemSlot>

    /**
     * 获取一个 [EquipmentSlot] 所对应的 [ItemSlot].
     * 如果不存在, 则返回 `null`.
     */
    fun get(slot: EquipmentSlot): ItemSlot?

    /**
     * 获取一个跟 [ItemSlot.slotIndex] 所对应的 [ItemSlot].
     * 如果不存在, 则返回 `null`.
     */
    fun get(slotIndex: Int): ItemSlot?

    /**
     * 注册一个 [ItemSlot] 实例.
     *
     * 每当一个 [ItemSlot] 被创建时, 应该调用此函数.
     *
     * @param slot [ItemSlot] 实例
     */
    fun register(slot: ItemSlot)
}