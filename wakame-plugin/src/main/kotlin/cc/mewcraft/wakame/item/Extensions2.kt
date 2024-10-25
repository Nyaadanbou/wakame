package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.StandaloneCell
import cc.mewcraft.wakame.item.components.cells.Cell

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

fun NekoStack.getStandaloneCell(): StandaloneCell? {
    return components.get(ItemComponentTypes.STANDALONE_CELL)
}

fun NekoStack.setStandaloneCell(cell: StandaloneCell) {
    components.set(ItemComponentTypes.STANDALONE_CELL, cell)
}