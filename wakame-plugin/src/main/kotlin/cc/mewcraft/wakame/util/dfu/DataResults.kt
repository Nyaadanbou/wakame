// Source: https://github.com/mcbrawls/codex/blob/latest/src/main/kotlin/dev/andante/codex/Codex.kt

package cc.mewcraft.wakame.util.dfu

import com.mojang.serialization.*
import java.util.*

/**
 * Converts a codec to a nullable codec, using of optional codecs as an intermediary.
 */
fun <A : Any> Codec<A>.nullableFieldOf(name: String): MapCodec<A?> {
    return optionalFieldOf(name).xmap({ it.orElse(null) }, { Optional.ofNullable(it) }).orElse(null)
}

/**
 * Converts a codec of a type to a codec of the same type but functional.
 */
fun <A : Any?> MapCodec<A>.functionally(): MapCodec<() -> A> {
    return xmap({ { it } }, { it() })
}

/**
 * Encodes to a dynamic ops format.
 */
fun <A, T> Encoder<A>.encodeQuick(ops: DynamicOps<T>, input: A): T? {
    return encodeStart(ops, input).result().orElse(null)
}

/**
 * Decodes from a dynamic ops format.
 */
fun <A, T> Decoder<A>.decodeQuick(ops: DynamicOps<T>, input: T): A? {
    return parse(ops, input).result().orElse(null)
}

object DataResults {
    /**
     * 把一个可空的对象转换为 [DataResult]. 如果不为空, 则返回 [DataResult.success], 否则返回 [DataResult.error].
     */
    fun <R> wrap(value: R?, partialResult: R? = null, error: () -> String): DataResult<R> {
        return if (value == null) {
            if (partialResult != null)
                DataResult.error(error, partialResult)
            else
                DataResult.error(error)
        } else {
            DataResult.success(value)
        }
    }
}
