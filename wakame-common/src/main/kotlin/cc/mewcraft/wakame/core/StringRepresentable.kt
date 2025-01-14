package cc.mewcraft.wakame.core

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import net.minecraft.Util
import net.minecraft.util.ExtraCodecs
import java.util.function.Supplier
import java.util.function.ToIntFunction

// 实现上可以使用 NMS 的 StringRepresentable.
// interface StringRepresentable {
//     val serializedName: String
// }

/**
 * 代表一个可以用 [String] 确定的对象.
 */
interface StringRepresentable {
    companion object {
        const val PRE_BUILT_MAP_THRESHOLD = 16

        fun <E> fromEnum(enumValues: Supplier<Array<E>>): EnumCodec<E> where E : Enum<E>, E : StringRepresentable {
            return fromEnumWithMapping(enumValues) { it }
        }

        fun <E> fromEnumWithMapping(
            enumValues: Supplier<Array<E>>,
            valueNameTransformer: (String) -> String,
        ): EnumCodec<E> where E : Enum<E>, E : StringRepresentable {
            val enums = enumValues.get()
            val function = createNameLookup(enums, valueNameTransformer)
            return EnumCodec(enums, function)
        }

        fun <T : StringRepresentable> fromValues(
            values: Supplier<Array<T>>,
        ): Codec<T> {
            val get = values.get()
            val function = createNameLookup(get) { it }
            val toIntFunction = Util.createIndexLookup(get.toList())
            return StringRepresentableCodec(get, function, toIntFunction)
        }

        fun <T : StringRepresentable> createNameLookup(
            values: Array<T>,
            valueNameTransformer: (String) -> String,
        ): (String) -> T? {
            if (values.size > PRE_BUILT_MAP_THRESHOLD) {
                val map = values.associateBy { representable -> valueNameTransformer(representable.stringId) }
                return map::get
            } else {
                return { name -> values.firstOrNull { valueNameTransformer(it.stringId) == name } }
            }
        }

        fun keys(values: Array<out StringRepresentable>): Keyable {
            return object : Keyable {
                override fun <T> keys(ops: DynamicOps<T>): Sequence<T> {
                    return values.asSequence().map(StringRepresentable::stringId).map(ops::createString)
                }
            }
        }
    }

    /**
     * 用于序列化的名称. 模必须符合模式 `[a-z0-9_]`.
     */
    val stringId: String

    class EnumCodec<E>(
        values: Array<E>,
        private val resolver: (String) -> E?,
    ) : StringRepresentableCodec<E>(
        values, resolver, { enum -> enum.ordinal }
    ) where E : Enum<E>, E : StringRepresentable {
        fun byName(id: String?): E? = id?.let(resolver)
        fun byName(id: String?, fallback: E): E = byName(id) ?: fallback
        fun byName(id: String?, fallback: () -> E): E = byName(id) ?: fallback()
    }

    open class StringRepresentableCodec<S : StringRepresentable>(
        values: Array<S>,
        nameToS: (String) -> S?,
        nameToOrdinal: ToIntFunction<S>,
    ) : Codec<S> {
        private val codec: Codec<S> = ExtraCodecs.orCompressed(
            Codec.stringResolver({ it?.stringId }, nameToS),
            ExtraCodecs.idResolverCodec(nameToOrdinal, { ordinal -> values.getOrNull(ordinal) }, -1)
        )

        override fun <T> decode(dynamicOps: DynamicOps<T>, any: T): DataResult<Pair<S, T>> {
            return codec.decode(dynamicOps, any)
        }

        override fun <T> encode(stringRepresetable: S, dynamicOps: DynamicOps<T>, prefix: T): DataResult<T> {
            return codec.encode(stringRepresetable, dynamicOps, prefix)
        }
    }
}
