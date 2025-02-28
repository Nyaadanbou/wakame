package cc.mewcraft.wakame.util

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import net.minecraft.Util
import net.minecraft.util.ExtraCodecs
import java.util.function.Supplier
import java.util.function.ToIntFunction

/**
 * An interface, usually implemented by enums, that allows the object to be serialized by codecs.
 * An instance is identified using a string.
 */
interface StringIdentifiable {

    companion object {

        private const val CACHED_MAP_THRESHOLD = 16

        fun <E> createCodec(
            enumValues: Supplier<Array<E>>,
        ): EnumCodec<E> where E : Enum<E>, E : StringIdentifiable {
            return createCodec(enumValues, { it })
        }

        fun <E> createCodec(
            enumValues: Supplier<Array<E>>,
            valueNameTransformer: (String) -> String,
        ): EnumCodec<E> where E : Enum<E>, E : StringIdentifiable {
            val enums = enumValues.get()
            val function = createMapper(enums, valueNameTransformer)
            return EnumCodec(enums, function)
        }

        fun <T : StringIdentifiable> createBasicCodec(
            values: Supplier<Array<T>>,
        ): Codec<T> {
            val get = values.get()
            val function = createMapper(get) { it }
            val toIntFunction = Util.createIndexLookup(get.toList())
            return BasicCodec(get, function, toIntFunction)
        }

        fun <T : StringIdentifiable> createMapper(
            values: Array<T>,
            valueNameTransformer: (String) -> String,
        ): (String) -> T? {
            if (values.size > CACHED_MAP_THRESHOLD) {
                val map = values.associateBy { representable -> valueNameTransformer(representable.stringId) }
                return { name -> map[name] }
            } else {
                return { name -> values.firstOrNull { valueNameTransformer(it.stringId) == name } }
            }
        }

        fun toKeyable(values: Array<out StringIdentifiable>): Keyable {
            return object : Keyable {
                override fun <T> keys(ops: DynamicOps<T>): Sequence<T> {
                    return values.asSequence().map(StringIdentifiable::stringId).map(ops::createString)
                }
            }
        }
    }

    /**
     * 用于序列化的名称. 模必须符合模式 `[a-z0-9_]`.
     */
    val stringId: String

    open class BasicCodec<S : StringIdentifiable>(
        values: Array<S>,
        nameToS: (String) -> S?,
        nameToOrdinal: ToIntFunction<S>,
    ) : Codec<S> {

        private val codec: Codec<S> = ExtraCodecs.orCompressed(
            Codec.stringResolver(
                { it?.stringId },
                nameToS
            ),
            ExtraCodecs.idResolverCodec(
                nameToOrdinal,
                { ordinal -> values.getOrNull(ordinal) },
                -1
            )
        )

        override fun <T> decode(dynamicOps: DynamicOps<T>, any: T): DataResult<Pair<S, T>> {
            return codec.decode(dynamicOps, any)
        }

        override fun <T> encode(stringRepresetable: S, dynamicOps: DynamicOps<T>, prefix: T): DataResult<T> {
            return codec.encode(stringRepresetable, dynamicOps, prefix)
        }

    }

    @Deprecated(message = "Why?")
    class EnumCodec<E> : BasicCodec<E> where E : Enum<E>, E : StringIdentifiable {

        private val resolver: (String) -> E?

        constructor(values: Array<E>, resolver: (String) -> E?) : super(
            values, resolver, Enum<E>::ordinal
        ) {
            this.resolver = resolver
        }

        fun byName(id: String?): E? = id?.let(this.resolver)
        fun byName(id: String?, fallback: E): E = this.byName(id) ?: fallback
        fun byName(id: String?, fallback: () -> E): E = this.byName(id) ?: fallback()

    }

}
