package cc.mewcraft.wakame.serialization.configurate

import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.RepresentationHint
import kotlin.reflect.KClass

/**
 * 存放整个 Koish 项目中所有存在的 [RepresentationHint].
 */
object RepresentationHints {

    @JvmField
    val KIZAMI_ID = of("kizami_id", Identifier::class)

    fun <T : Any> of(key: String, type: KClass<T>): RepresentationHint<T> {
        return RepresentationHint.of(key, type.java)
    }

    inline fun <reified T : Any> of(key: String): RepresentationHint<T> {
        return RepresentationHint.of(key, typeTokenOf())
    }
}