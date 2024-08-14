package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 工作站.
 */
sealed interface Station : Iterable<StationRecipe> {
    val id: String
    fun addRecipe(recipe: StationRecipe): Boolean
    fun removeRecipe(key: Key): StationRecipe?

}

/**
 * 无分类工作站的实现.
 */
class SimpleStation(
    override val id: String
) : Station, KoinComponent {
    val logger: Logger by inject()

    private val recipes: MutableMap<Key, StationRecipe> = mutableMapOf()

    override fun addRecipe(recipe: StationRecipe): Boolean {
        val success = recipes.putIfAbsent(recipe.key, recipe)
        if (success != null) {
            logger.warn("Duplicate key. Station recipe will not be added.")
            return false
        }
        return true
    }

    override fun removeRecipe(key: Key): StationRecipe? {
        return recipes.remove(key)
    }

    override fun iterator(): Iterator<StationRecipe> {
        return recipes.values.iterator()
    }
}

/**
 * [Station] 的序列化器.
 */
internal object StationSerializer : TypeSerializer<Station> {
    val HINT_NODE: RepresentationHint<String> = RepresentationHint.of("id", typeTokenOf<String>())
    override fun deserialize(type: Type, node: ConfigurationNode): Station {
        val id = node.hint(HINT_NODE) ?: throw SerializationException(
            "The hint node for station id is not present"
        )
        return SimpleStation(id)
    }
}