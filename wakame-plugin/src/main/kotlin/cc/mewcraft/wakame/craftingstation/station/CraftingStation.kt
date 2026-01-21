package cc.mewcraft.wakame.craftingstation.station

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.gui.BasicMenuSettings
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
    val primaryMenuSettings: BasicMenuSettings
    val previewMenuSettings: BasicMenuSettings
    val catalogMenuSettings: BasicMenuSettings
    fun addRecipe(recipe: Recipe): Boolean
    fun removeRecipe(key: Key): Recipe?
}

/**
 * 无分类合成站的实现.
 */
internal class SimpleCraftingStation(
    override val id: String,
    override val primaryMenuSettings: BasicMenuSettings,
    override val previewMenuSettings: BasicMenuSettings,
    override val catalogMenuSettings: BasicMenuSettings,
) : CraftingStation {
    companion object {
        const val TYPE = "simple"
    }

    private val recipes: MutableMap<Key, Recipe> = mutableMapOf()

    override fun addRecipe(recipe: Recipe): Boolean {
        val success = recipes.putIfAbsent(recipe.identifier, recipe)
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
internal object StationSerializer : SimpleSerializer<CraftingStation> {
    val HINT_NODE: RepresentationHint<String> = RepresentationHint.of("id", typeTokenOf<String>())
    override fun deserialize(type: Type, node: ConfigurationNode): CraftingStation {
        val id = node.hint(HINT_NODE) ?: throw SerializationException("the hint node for station id is not present")
        val stationType = node.node("type").require<String>()
        when (stationType) {
            SimpleCraftingStation.TYPE -> {
                // 获取合成站菜单布局
                val stationMenuSettings = node.node("listing_menu_settings").require<BasicMenuSettings>()
                // 获取合成站预览菜单布局
                val previewMenuSettings = node.node("preview_menu_settings").require<BasicMenuSettings>()
                // 获取合成站在图鉴中展示时使用的图鉴菜单布局
                val catalogMenuSettings = node.node("catalog_menu_settings").require<BasicMenuSettings>()

                val station = SimpleCraftingStation(id, stationMenuSettings, previewMenuSettings, catalogMenuSettings)

                // 向合成站添加配方
                val recipeKeys = node.node("recipes").getList<Key>(emptyList())
                for (key in recipeKeys) {
                    val stationRecipe = if (SharedConstants.isRunningInIde) {
                        CraftingStationRegistry.getRawRecipe(key)
                    } else {
                        CraftingStationRegistry.getRecipe(key)
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