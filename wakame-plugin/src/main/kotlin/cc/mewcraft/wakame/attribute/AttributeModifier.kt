package cc.mewcraft.wakame.attribute

import java.util.*

/**
 * An [AttributeModifier] is responsible to modify an [Attribute]. By
 * design, all the instance creators of [AttributeModifier] must provide a
 * [UUID], which is then used to distinguish the "source" of the modifier.
 */
data class AttributeModifier(
    val id: UUID,
    val name: String? = null,
    val amount: Double,
    val operation: Operation,
) {

    constructor(id: UUID, amount: Double, operation: Operation) : this(id, null, amount, operation)

    enum class Operation(
        val key: String,
        val id: Int,
    ) {
        ADDITION("add", 0),
        MULTIPLY_BASE("multiply_base", 1),
        MULTIPLY_TOTAL("multiply_total", 2);

        val binary: Byte = id.toByte()

        companion object {
            private val OPERATIONS_ARRAY = arrayOf(
                ADDITION,
                MULTIPLY_BASE,
                MULTIPLY_TOTAL
            )
            private val OPERATIONS_MAP = mapOf(
                ADDITION.key to ADDITION,
                MULTIPLY_BASE to MULTIPLY_BASE,
                MULTIPLY_TOTAL to MULTIPLY_TOTAL
            )

            fun byKey(key: String): Operation {
                return requireNotNull(OPERATIONS_MAP[key]) { "No operation with key $key" }
            }

            fun byId(id: Int): Operation {
                return if (id >= 0 && id < OPERATIONS_ARRAY.size) {
                    OPERATIONS_ARRAY[id]
                } else {
                    throw IllegalArgumentException("No operation with id $id")
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AttributeModifier
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}