package cc.mewcraft.wakame.attribute

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap
import net.kyori.adventure.key.Key

/**
 * An [AttributeModifier] is responsible to modify an [Attribute]. By
 * design, all the instance creators of [AttributeModifier] must provide a
 * [Key], which is then used to distinguish the "source" of the modifier.
 */
data class AttributeModifier(
    val id: Key,
    val amount: Double,
    val operation: Operation,
) {
    enum class Operation(
        val id: Int,
        val key: String,
    ) {
        ADD(0, "add"),
        MULTIPLY_BASE(1, "multiply_base"),
        MULTIPLY_TOTAL(2, "multiply_total");

        val binary: Byte = id.toByte()

        companion object {
            private val BY_NAME: Map<String, Operation> = mapOf(
                ADD.key to ADD,
                MULTIPLY_BASE.key to MULTIPLY_BASE,
                MULTIPLY_TOTAL.key to MULTIPLY_TOTAL
            ).let(::Object2ReferenceArrayMap)

            fun byId(id: Int): Operation? {
                return entries.getOrNull(id)
            }

            fun byName(key: String): Operation? {
                return BY_NAME[key]
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is AttributeModifier)
            return id == other.id // 原版无法重复添加 id 相同的属性修饰符, 因此只需要比较 id 即可
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}