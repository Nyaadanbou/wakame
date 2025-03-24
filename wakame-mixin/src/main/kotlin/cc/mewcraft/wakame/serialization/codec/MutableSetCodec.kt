package cc.mewcraft.wakame.serialization.codec

import com.mojang.datafixers.util.Pair
import com.mojang.datafixers.util.Unit
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Lifecycle
import org.apache.commons.lang3.mutable.MutableObject
import java.util.*
import java.util.stream.Stream

fun <A> Codec<A>.mutableSetOf(): MutableSetCodec<A> {
    return MutableSetCodec(this)
}

class MutableSetCodec<A>(private val elementCodec: Codec<A>) : Codec<MutableSet<A>> {

    override fun <T : Any?> encode(input: MutableSet<A>, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        val builder = ops.listBuilder()

        for (a in input) {
            builder.add(elementCodec.encodeStart(ops, a))
        }

        return builder.build(prefix)
    }

    override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<MutableSet<A>, T>> {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap { stream ->
            val read = mutableSetOf<A>()
            val failed = Stream.builder<T>()
            val result = MutableObject<DataResult<Unit>>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()))

            stream.accept { t ->
                val element = elementCodec.decode(ops, t)
                element.error().ifPresent { failed.add(t) }
                result.value = result.value.apply2stable({ r, v ->
                    read.add(v.first)
                    return@apply2stable r
                }, element)
            }

            val elements = read.toMutableSet()
            val errors = ops.createList(failed.build())

            val pair = Pair.of(elements, errors)

            return@flatMap result.value.map { pair }.setPartial(pair)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val setCodec = other as MutableSetCodec<*>
        return elementCodec == setCodec.elementCodec
    }

    override fun hashCode(): Int {
        return Objects.hash(elementCodec)
    }

    override fun toString(): String {
        return "MutableSetCodec[$elementCodec]"
    }

}