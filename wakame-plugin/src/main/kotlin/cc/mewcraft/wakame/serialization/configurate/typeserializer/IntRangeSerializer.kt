package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.util.RangeParser
import cc.mewcraft.wakame.util.typeTokenOf
import com.google.common.collect.Range
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

internal object IntRangeSerializer : ScalarSerializer<Range<Int>>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): Range<Int> {
        return RangeParser.parseIntRange(obj.toString())
    }

    override fun serialize(item: Range<Int>?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}