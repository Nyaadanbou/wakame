package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.AbilityBinaryValue
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import com.google.common.collect.Multimap
import me.lucko.helper.shadows.nbt.CompoundShadowTag

interface CellAccessor : CellSetter {
    /**
     * Encompassing all tags of this [CellAccessor].
     */
    val tags: CompoundShadowTag // 外部不应该读取该变量

    /**
     * Gets an immutable map describing the cells. This will call [get] for
     * each available cell on the backed item.
     *
     * Note that any changes on the item in the underlying game world **does
     * not** reflect on the returned value.
     */
    fun asMap(): Map<String, BinaryCell>

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
     * Gets all attribute modifiers from `this`.
     */
    fun getModifiers(): Multimap<out Attribute, AttributeModifier>

    /**
     * Gets all abilities and corresponding values from `this`.
     */
    fun getAbilities(): Map<out Ability, AbilityBinaryValue>
}

