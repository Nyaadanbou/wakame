package cc.mewcraft.wakame.attribute

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap
import java.util.UUID

/**
 * An [AttributeModifier] is responsible to modify an [Attribute]. By
 * design, all the instance creators of [AttributeModifier] must provide a
 * [UUID], which is then used to distinguish the "source" of the modifier.
 */
data class AttributeModifier(
    val id: UUID,
    val amount: Double,
    val operation: Operation,
) {
    enum class Operation(
        val key: String,
        val id: Int,
    ) {
        ADD("add", 0),
        MULTIPLY_BASE("multiply_base", 1),
        MULTIPLY_TOTAL("multiply_total", 2);

        val binary: Byte = id.toByte()

        companion object {
            private val BY_KEY: Map<String, Operation> = mapOf(
                ADD.key to ADD,
                MULTIPLY_BASE.key to MULTIPLY_BASE,
                MULTIPLY_TOTAL.key to MULTIPLY_TOTAL
            ).let(::Object2ReferenceArrayMap)

            fun byKey(key: String): Operation? {
                return BY_KEY[key]
            }

            fun byKeyOrThrow(key: String): Operation {
                return requireNotNull(byKey(key)) { "Can't find operation with key '$key'" }
            }

            fun byId(id: Int): Operation? {
                return entries.getOrNull(id)
            }

            fun byIdOrThrow(id: Int): Operation {
                return byId(id) ?: throw IllegalArgumentException("Can't find operation with id '$id'")
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is AttributeModifier)
            return id == other.id && operation == other.operation
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode() * 31 + operation.hashCode()
    }
}