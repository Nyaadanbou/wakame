package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * 合成站.
 */
internal sealed interface CraftingStation : Iterable<Recipe> {
    val id: String
    val primaryLayout: BasicMenuSettings
    val previewLayout: BasicMenuSettings
    fun addRecipe(recipe: Recipe): Boolean
    fun removeRecipe(key: Key): Recipe?
}

/**
 * 无分类合成站的实现.
 */
internal class SimpleCraftingStation(
    override val id: String,
    override val primaryLayout: BasicMenuSettings,
    override val previewLayout: BasicMenuSettings,
) : CraftingStation {
    companion object {
        const val TYPE = "simple"
        val STATION_STRUCTURE_LEGAL_CHARS = charArrayOf('x', '.', '<', '>')
        val PREVIEW_STRUCTURE_LEGAL_CHARS = charArrayOf('x', 'i', 'o', 'c', 'b', '<', '>')
    }

    private val recipes: MutableMap<Key, Recipe> = mutableMapOf()

    override fun addRecipe(recipe: Recipe): Boolean {
        val success = recipes.putIfAbsent(recipe.key, recipe)
        if (success != null) {
            LOGGER.warn("Duplicate key. Station recipe will not be added.")
            return false
        }
        return true
    }

    override fun removeRecipe(key: Key): Recipe? {
        return recipes.remove(key)
    }

    override fun iterator(): Iterator<Recipe> {
        return recipes.values.iterator()
    }
}

/**
 * [CraftingStation] 的序列化器.
 */
internal object StationSerializer : TypeSerializer<CraftingStation> {
    val HINT_NODE: RepresentationHint<String> = RepresentationHint.of("id", typeTokenOf<String>())
    override fun deserialize(type: Type, node: ConfigurationNode): CraftingStation {
        val id = node.hint(HINT_NODE) ?: throw SerializationException("the hint node for station id is not present")
        val stationType = node.node("type").krequire<String>()
        when (stationType) {
            SimpleCraftingStation.TYPE -> {
                // 获取合成站菜单布局
                val stationLayout = node.node("layout").krequire<BasicMenuSettings>().apply {
                    val illegalChars = this.structure.map { it.toCharArray() }
                        .reduce { acc, chars -> acc + chars }
                        .distinct()
                        .filter { !SimpleCraftingStation.STATION_STRUCTURE_LEGAL_CHARS.contains(it) && it != ' ' }
                    if (illegalChars.isNotEmpty()) {
                        throw SerializationException("the chars [${illegalChars.joinToString(separator = ", ", prefix = "'", postfix = "'")}] are illegal in the station menu structure")
                    }
                }

                // 获取合成站预览菜单布局
                val previewLayout = node.node("preview_layout").krequire<BasicMenuSettings>().apply {
                    val illegalChars = this.structure.map { it.toCharArray() }
                        .reduce { acc, chars -> acc + chars }
                        .distinct()
                        .filter { !SimpleCraftingStation.PREVIEW_STRUCTURE_LEGAL_CHARS.contains(it) && it != ' ' }
                    if (illegalChars.isNotEmpty()) {
                        throw SerializationException("the chars [${illegalChars.joinToString(separator = ", ", prefix = "'", postfix = "'")}] are illegal in the station preview menu structure")
                    }
                }

                val station = SimpleCraftingStation(id, stationLayout, previewLayout)

                // 向合成站添加配方
                val recipeKeys = node.node("recipes").getList<Key>(emptyList())
                for (key in recipeKeys) {
                    val stationRecipe = if (RunningEnvironment.TEST.isRunning()) {
                        CraftingStationRecipeRegistry.raw[key]
                    } else {
                        CraftingStationRecipeRegistry[key]
                    }
                    if (stationRecipe == null) {
                        LOGGER.warn("Can't find station recipe: '$key'. Skip adding it to station: '$id'")
                        continue
                    }
                    station.addRecipe(stationRecipe)
                }

                return station
            }

            else -> {
                throw SerializationException("unknown station type: $stationType")
            }
        }
    }
}