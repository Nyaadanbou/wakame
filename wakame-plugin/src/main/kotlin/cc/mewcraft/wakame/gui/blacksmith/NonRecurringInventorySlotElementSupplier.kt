package cc.mewcraft.wakame.gui.blacksmith

import xyz.xenondevs.invui.gui.SlotElement.*
import xyz.xenondevs.invui.inventory.Inventory
import xyz.xenondevs.invui.item.ItemProvider
import java.util.function.Supplier

class NonRecurringInventorySlotElementSupplier(
    private val inventory: Inventory,
    private val background: ItemProvider? = null,
) : Supplier<InventorySlotElement?> {
    private var slot = -1

    override fun get(): InventorySlotElement? {
        if (++slot >= inventory.size)
            return null
        return InventorySlotElement(inventory, slot, background)
    }
}