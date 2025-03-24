package cc.mewcraft.wakame.serialization.codec

import com.mojang.serialization.Codec
import org.joml.Vector2f
import org.joml.Vector3d
import kotlin.time.Duration

/**
 * 通用数据类型的 Codec, 可独立于 Koish.
 */
object ExtraCodecs {

    @JvmField
    val VECTOR_3D: Codec<Vector3d> = Codec.DOUBLE.listOf().xmap(
        { list -> Vector3d(list[0], list[1], list[2]) },
        { vect -> listOf(vect.x, vect.y, vect.z) }
    )

    @JvmField
    val VECTOR_2F: Codec<Vector2f> = Codec.FLOAT.listOf().xmap(
        { list -> Vector2f(list[0], list[1]) },
        { vect -> listOf(vect.x, vect.y) }
    )

    @JvmField
    val DURATION: Codec<Duration> = Codec.STRING.xmap(Duration::parse, Duration::toString)

    fun <F, S> nativePair(first: Codec<F>, second: Codec<S>): Codec<Pair<F, S>> = Codec.pair(first, second)
        .xmap(
            { it.first to it.second },
            { com.mojang.datafixers.util.Pair.of(it.first, it.second) }
        )

}