package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.Trigger
import com.google.common.collect.Multimap

/**
 * This is the interface to get the [cell holder][BinaryCell] on the ItemStack.
 */
interface ItemCellAccessor {

    /* Getters */

    /**
     * Gets an immutable map describing **all** the cells in this holder, neglecting
     * what curses the cells own.
     *
     * Note that although the map is immutable, the entries in the map is not!
     *
     * @see BinaryCell
     */
    val snapshot: Map<String, BinaryCell>

    /**
     * Gets the specified [binary cell][BinaryCell].
     *
     * Note that any changes on the item in the underlying game world **does
     * not** reflect on the returned value.
     *
     * @param id the ID of the cell (case-sensitive)
     * @return the specified binary cell or `null`, if not found
     */
    fun find(id: String): BinaryCell?

    /**
     * Gets the specified [binary cell][BinaryCell].
     *
     * @param id the ID of the cell (case-sensitive)
     * @return the specified binary cell
     * @throws IllegalArgumentException if the specified binary cell is not found
     */
    fun get(id: String): BinaryCell = requireNotNull(find(id)) { "Can't find binary cell for $id" }

    /**
     * Returns `true` if the specified cell exists.
     *
     * @param id the identifier of the cell (case-sensitive)
     * @return true if the specified cell exists
     */
    fun contains(id: String): Boolean = find(id) != null

    /**
     * Gets all attribute modifiers from all the cells.
     */
    fun getAttributeModifiers(neglectCurse: Boolean = false): Multimap<Attribute, AttributeModifier>

    /**
     * Gets all skills from all the cells.
     */
    fun getSkills(neglectCurse: Boolean = false, neglectVariant: Boolean = false): Multimap<Trigger, Skill>

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
     * aligned with the number of cells in its schema.
     *
     * @param id the cell ID (case-sensitive)
     */
    fun remove(id: String)

}