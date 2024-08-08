package cc.mewcraft.wakame.station

import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger


class Station(
    val id: String
) : KoinComponent {
    val logger: Logger by inject()

    private val recipes: MutableMap<Key, StationRecipe> = mutableMapOf()

    fun addRecipe(recipe: StationRecipe): Boolean {
        val success = recipes.putIfAbsent(recipe.key, recipe)
        if (success != null) {
            logger.warn("Duplicate key. Station recipe will not be added.")
            return false
        }
        return true
    }

    fun removeRecipe(key: Key): StationRecipe? {
        return recipes.remove(key)
    }
}