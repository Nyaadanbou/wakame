package cc.mewcraft.wakame.attribute

/**
 * This is not an Attribute!
 *
 * An attribute modifier applied to [Attribute] on players.
 */
class AttributeModifier(
    val amount: Number,
    val operation: Operation,
    val name: String? = null,
) {
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
}
