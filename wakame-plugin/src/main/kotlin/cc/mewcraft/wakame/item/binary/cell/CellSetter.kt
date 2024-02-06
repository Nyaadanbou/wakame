package cc.mewcraft.wakame.item.binary.cell

interface CellSetter {
    /**
     * Adds a cell.
     *
     * @param id the cell ID (case-sensitive)
     * @param cell the cell to be added
     */
    fun put(id: String, cell: BinaryCell)

    /**
     * Edits a cell (i.e., replacing the existing one with the new cell
     * returned by [setter]). If the specified cell does not exist,
     * this function is effectively the same as the function [put].
     *
     * @param id the cell ID (case-sensitive)
     * @param setter the cell setter function
     * @receiver the existing cell or `null`, if the specified cell does not
     *     already exist
     */
    fun edit(id: String, setter: BinaryCell?.() -> BinaryCell)

    /**
     * Removes the specified [binary cell][BinaryCell]. This will entirely
     * remove the cell AND its core from the item NBT, leading to this item not
     * aligned with the number of cells in its scheme.
     *
     * @param id the cell ID (case-sensitive)
     */
    fun remove(id: String)
}