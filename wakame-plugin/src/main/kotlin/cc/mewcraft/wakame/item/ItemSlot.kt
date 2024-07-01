package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.ItemSlot.NoopItemSlot
import net.kyori.examination.Examinable
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot


/**
 * Represents an inventory slot which is potentially effective for an item.
 *
 * If an item is **in** the effective slot, the item should be considered "effective"
 * so that all the attributes, abilities, kizamis, etc. should become effective
 * for the current item holder, i.e., the player.
 *
 * If an item has no effective slot, use the [NoopItemSlot] singleton.
 */
interface ItemSlot : Examinable {
    /*
     * Raw Slots:
     *
     * 5             1  2     0
     * 6             3  4
     * 7
     * 8           45
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 36 37 38 39 40 41 42 43 44
     */

    /*
     * Converted Slots:
     *
     * 39             1  2     0
     * 38             3  4
     * 37
     * 36          40
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 0  1  2  3  4  5  6  7  8
     */

    /**
     * Checks if the given parameters is referring to the effective slot.
     *
     * This function is used to check whether the event leads to an effective slot for the item.
     */
    fun testItemHeldEvent(player: Player, previousSlot: Int, newSlot: Int): Boolean = false // default returns false

    /**
     * Checks if the given parameters is referring to the effective slot.
     *
     * This function is used to check whether the event leads to an effective slot for the item.
     */
    fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean = false // default returns false

    /**
     * Checks if the given parameters is referring to the effective slot.
     *
     * This function is used to check whether the equipment slot is an effective slot for the item.
     */
    fun testEquipmentSlot(slot: EquipmentSlot): Boolean = false // default returns false

    companion object {
        /**
         * 获取一个无操作 [ItemSlot].
         */
        fun noop(): ItemSlot {
            return NoopItemSlot
        }
    }

    /**
     * This is the effective slot that never takes effect.
     *
     * Used for items that provides no effects for players, such as materials.
     */
    private data object NoopItemSlot : ItemSlot
}
