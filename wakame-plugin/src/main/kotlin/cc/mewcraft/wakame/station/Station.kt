package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.station.recipe.StationRecipe
import cc.mewcraft.wakame.station.recipe.StationRecipeRegistry
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 合成站.
 */
sealed interface Station : Iterable<StationRecipe> {
    val id: String
    val layout: StationLayout
    fun addRecipe(recipe: StationRecipe): Boolean
    fun removeRecipe(key: Key): StationRecipe?

}

/**
 * 无分类合成站的实现.
 */
class SimpleStation(
    override val id: String,
    override val layout: StationLayout
) : Station, KoinComponent {
    companion object {
        const val TYPE = "simple"
    }

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
internal object StationSerializer : TypeSerializer<Station>, KoinComponent {
    private val logger: Logger by inject()
    val HINT_NODE: RepresentationHint<String> = RepresentationHint.of("id", typeTokenOf<String>())
    override fun deserialize(type: Type, node: ConfigurationNode): Station {
        val id = node.hint(HINT_NODE) ?: throw SerializationException(
            "The hint node for station id is not present"
        )
        val stationType = node.node("type").krequire<String>()
        when (stationType) {
            SimpleStation.TYPE -> {
                // 获取合成站布局
                val stationLayout = node.node("layout").krequire<StationLayout>().apply {
                    require(this.isSimple) {"Simple station should use 0~5 in structure"}
                }

                val station = SimpleStation(id, stationLayout)

                // 向合成站添加配方
                val recipeKeys = node.node("recipes").getList<Key>(emptyList())
                for (key in recipeKeys) {
                    val stationRecipe = if (RunningEnvironment.TEST.isRunning()) {
                        StationRecipeRegistry.raw[key]
                    } else {
                        StationRecipeRegistry.find(key)
                    }
                    if (stationRecipe == null) {
                        logger.warn("Can't find station recipe: '$key'. Skip add it to station: '$id'")
                        continue
                    }
                    station.addRecipe(stationRecipe)
                }

                return station
            }

            else -> {
                throw SerializationException("Unknown station type")
            }
        }
    }
}