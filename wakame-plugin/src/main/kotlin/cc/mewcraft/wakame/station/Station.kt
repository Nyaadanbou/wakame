package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.station.recipe.StationRecipe
import cc.mewcraft.wakame.station.recipe.StationRecipeRegistry
import cc.mewcraft.wakame.util.*
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
internal sealed interface Station : Iterable<StationRecipe> {
    val id: String
    val layout: MenuLayout
    val previewLayout: MenuLayout
    fun addRecipe(recipe: StationRecipe): Boolean
    fun removeRecipe(key: Key): StationRecipe?
}

/**
 * 无分类合成站的实现.
 */
internal class SimpleStation(
    override val id: String,
    override val layout: MenuLayout,
    override val previewLayout: MenuLayout
) : Station, KoinComponent {
    companion object {
        const val TYPE = "simple"
        val STATION_STRUCTURE_LEGAL_CHARS = charArrayOf('X', '.', '<', '>')
        val PREVIEW_STRUCTURE_LEGAL_CHARS = charArrayOf('X', 'I', 'O', 'C', 'B', '<', '>')
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
                // 获取合成站菜单布局
                val stationLayout = node.node("layout").krequire<MenuLayout>().apply {
                    val illegalChars = this.structure.map { it.toCharArray() }
                        .reduce { acc, chars -> acc + chars }
                        .distinct()
                        .filter { !SimpleStation.STATION_STRUCTURE_LEGAL_CHARS.contains(it) && it != ' ' }
                    if (illegalChars.isNotEmpty()) {
                        throw SerializationException("The chars [${illegalChars.joinToString(separator = ", ", prefix = "'", postfix = "'")}] are illegal in the station menu structure")
                    }
                }

                // 获取合成站预览菜单布局
                val previewLayout = node.node("preview_layout").krequire<MenuLayout>().apply {
                    val illegalChars = this.structure.map { it.toCharArray() }
                        .reduce { acc, chars -> acc + chars }
                        .distinct()
                        .filter { !SimpleStation.PREVIEW_STRUCTURE_LEGAL_CHARS.contains(it) && it != ' ' }
                    if (illegalChars.isNotEmpty()) {
                        throw SerializationException("The chars [${illegalChars.joinToString(separator = ", ", prefix = "'", postfix = "'")}] are illegal in the station preview menu structure")
                    }
                }

                val station = SimpleStation(id, stationLayout, previewLayout)

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