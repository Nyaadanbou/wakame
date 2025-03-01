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
    val KIZAMI_ID = create<Identifier>("kizami_id")

    @JvmField
    val CATAGORY_ID = create<Identifier>("category_id")

    @JvmField
    val MINECRAFT_RECIPE_ID = create<Identifier>("minecraft_recipe_id")

    private fun <T : Any> create(key: String, type: KClass<T>): RepresentationHint<T> {
        return RepresentationHint.of(key, type.java)
    }

    private inline fun <reified T : Any> create(key: String): RepresentationHint<T> {
        return RepresentationHint.of(key, typeTokenOf())
    }
}