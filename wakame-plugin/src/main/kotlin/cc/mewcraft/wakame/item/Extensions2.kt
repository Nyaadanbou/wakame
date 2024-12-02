package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.ItemRarity
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import kotlin.reflect.KProperty

var NekoStack.level: Int
    get() = components.get(ItemComponentTypes.LEVEL)?.level ?: ItemLevel.minimumLevel
    set(value) {
        require(value > 0) { "Level must be positive" }
        val boxedValue = ItemLevel(value)
        components.set(ItemComponentTypes.LEVEL, boxedValue)
    }

var NekoStack.rarity: Rarity
    get() = components.get(ItemComponentTypes.RARITY)?.rarity ?: RarityRegistry.DEFAULT
    set(value) {
        val boxedValue = ItemRarity(value)
        components.set(ItemComponentTypes.RARITY, boxedValue)
    }

var NekoStack.reforgeHistory: ReforgeHistory
    get() = components.get(ItemComponentTypes.REFORGE_HISTORY) ?: ReforgeHistory.ZERO
    set(value) = components.set(ItemComponentTypes.REFORGE_HISTORY, value)

var NekoStack.cells: ItemCells? by direct(ItemComponentTypes.CELLS)

fun NekoStack.getCell(id: String): Cell? {
    return components.get(ItemComponentTypes.CELLS)?.get(id)
}

fun NekoStack.putCell(id: String, cell: Cell) {
    val changed = components.get(ItemComponentTypes.CELLS)?.put(id, cell)
    if (changed != null) {
        components.set(ItemComponentTypes.CELLS, changed)
    }
}

fun NekoStack.modifyCell(id: String, block: Cell.() -> Cell) {
    val changed = components.get(ItemComponentTypes.CELLS)?.modify(id, block)
    if (changed != null) {
        components.set(ItemComponentTypes.CELLS, changed)
    }
}

fun NekoStack.removeCell(id: String) {
    val changed = components.get(ItemComponentTypes.CELLS)?.remove(id)
    if (changed != null) {
        components.set(ItemComponentTypes.CELLS, changed)
    }
}

var NekoStack.standaloneCell: StandaloneCell? by direct(ItemComponentTypes.STANDALONE_CELL)

var NekoStack.portableCore: PortableCore? by direct(ItemComponentTypes.PORTABLE_CORE)


/* private */


private fun <T> direct(type: ItemComponentType<T>): DirectComponentDelegate<T> {
    return DirectComponentDelegate(type)
}

private class DirectComponentDelegate<T>(
    private val type: ItemComponentType<T>,
) {
    operator fun getValue(thisRef: NekoStack, property: KProperty<*>): T? {
        return thisRef.components.get(type)
    }

    operator fun setValue(thisRef: NekoStack, property: KProperty<*>, value: T?) {
        if (value == null) {
            thisRef.components.unset(type)
        } else {
            thisRef.components.set(type, value)
        }
    }
}