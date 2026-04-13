package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.StringRepresentable
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * An [AttributeModifier] is responsible to conceptually modify an [Attribute].
 * By design, all the instance creators of [AttributeModifier] must provide a
 * [Key], which is then used to distinguish the "source" of the modifier.
 */
data class AttributeModifier(
    val id: Key,
    val amount: Double,
    val operation: Operation,
) {
    companion object {
        internal fun serializer(): SimpleSerializer<AttributeModifier> {
            return object : SimpleSerializer<AttributeModifier> {
                override fun deserialize(type: Type, node: ConfigurationNode): AttributeModifier {
                    val id = node.node("id").require<KoishKey>()
                    val operation = node.node("operation").require<AttributeModifier.Operation>()
                    val value = node.node("value").require<Double>()
                    return AttributeModifier(id, value, operation)
                }

                override fun serialize(type: Type, obj: AttributeModifier?, node: ConfigurationNode) {
                    if (obj == null) return
                    val id = obj.id
                    val operation = obj.operation
                    val amount = obj.amount
                    node.node("id").set(id)
                    node.node("operation").set(operation)
                    node.node("value").set(amount)
                }
            }
        }
    }

    enum class Operation(
        val id: Int,
        val key: String,
    ) : StringRepresentable {
        ADD(0, "add"),
        MULTIPLY_BASE(1, "multiply_base"),
        MULTIPLY_TOTAL(2, "multiply_total");

        override fun serializedName(): String = this.key

        companion object {
            private val BY_NAME: Map<String, Operation> = mapOf(
                ADD.key to ADD,
                MULTIPLY_BASE.key to MULTIPLY_BASE,
                MULTIPLY_TOTAL.key to MULTIPLY_TOTAL
            ).let(::Object2ReferenceArrayMap)

            fun byId(id: Int): Operation? = entries.getOrNull(id)

            fun byName(key: String): Operation? = BY_NAME[key]

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