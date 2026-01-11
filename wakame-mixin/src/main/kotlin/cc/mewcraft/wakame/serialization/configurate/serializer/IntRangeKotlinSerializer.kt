package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.util.toKotlinRange
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

/*internal*/ object IntRangeKotlinSerializer : ScalarSerializer<IntRange>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): IntRange? {
        val range = IntRangeGuavaSerializer.deserialize(type, obj)
        return range.toKotlinRange()
    }

    override fun serialize(item: IntRange?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}