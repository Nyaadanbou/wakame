package cc.mewcraft.wakame.loot.entry

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.lang.reflect.ParameterizedType
import java.util.stream.Stream

class SimpleEntry<S>(
    val data: S,
    weight: Int,
    quality: Int,
    conditions: List<LootPredicate>,
) : LootPoolSingletonContainer<S>(weight, quality, conditions), Examinable {
    companion object {
        val SERIALIZER: SimpleSerializer<SimpleEntry<*>> = SimpleSerializer { type, node ->
            val (weight, quality, conditions) = commonFields(node)
            val type = type as ParameterizedType
            val sType = type.actualTypeArguments[0]
            val data = node.node("data").require(sType)

            SimpleEntry(data, weight, quality, conditions)
        }
    }

    override fun createData(context: LootContext, dataConsumer: (S) -> Unit) {
        dataConsumer.invoke(data)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("data", data),
            ExaminableProperty.of("weight", weight),
            ExaminableProperty.of("quality", quality),
            ExaminableProperty.of("conditions", conditions)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}