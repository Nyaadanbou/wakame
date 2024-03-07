package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.BinaryAbilityValue
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import com.google.common.collect.Multimap

interface ItemCellHolder {

    /* Getters */

    /**
     * Gets an immutable map describing the cells in this holder. This will
     * call [get] for each available cell on the backed item.
     *
     * Note that any changes on the item in the underlying game world **does
     * not** reflect on the returned map.
     */
    val map: Map<String, BinaryCell>

    /**
     * Gets the specified [binary cell][BinaryCell].
     *
     * Note that any changes on the item in the underlying game world **does
     * not** reflect on the returned value.
     *
     * @param id the ID of the cell (case-sensitive)
     * @return the specified binary cell or `null`, if not found
     */
    fun get(id: String): BinaryCell?

    /**
     * Gets the specified [binary cell][BinaryCell].
     *
     * @param id the ID of the cell (case-sensitive)
     * @return the specified binary cell
     * @throws NullPointerException if the specified binary cell is not found
     */
    fun getOrThrow(id: String): BinaryCell = requireNotNull(get(id)) { "Can't find binary cell for $id" }

    /**
     * Returns `true` if the specified cell exists.
     *
     * @param id the identifier of the cell (case-sensitive)
     * @return true if the specified cell exists
     */
    fun contains(id: String): Boolean = get(id) != null

    /**
     * Gets all attribute modifiers from `this` (cells).
     */
    fun getModifiers(): Multimap<out Attribute, AttributeModifier>

    /**
     * Gets all abilities and corresponding values from `this` (cells).
     */
    fun getAbilities(): Map<out Ability, BinaryAbilityValue>

    /* Setters */

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

