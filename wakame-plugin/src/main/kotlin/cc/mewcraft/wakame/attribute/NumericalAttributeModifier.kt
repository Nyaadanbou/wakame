package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key

/**
 * A modifier applied to [NumericalAttribute] on players.
 */
class NumericalAttributeModifier(
    override val attributeKey: Key,
    val amount: Double = 0.0,
    val operation: Operation,
    val nameGetter: () -> String?,
) : Attribute {
}

enum class Operation(
    private val key: String,
    private val id: Int,
) {
    ADDITION("addition", 0),
    MULTIPLY_BASE("multiply_base", 1),
    MULTIPLY_TOTAL("multiply_total", 2);

    companion object {
        private val OPERATIONS = arrayOf(ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL)

        fun fromValue(id: Int): Operation {
            return if (id >= 0 && id < OPERATIONS.size) {
                OPERATIONS[id]
            } else {
                throw IllegalArgumentException("No operation with value $id")
            }
        }

    }
}
