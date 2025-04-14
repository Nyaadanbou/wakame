package cc.mewcraft.wakame.item2.data.impl

import org.bukkit.Color
import org.spongepowered.configurate.objectmapping.ConfigSerializable

// We're not using BreweryX's BRecipe class because it has a bunch of extra stuff that we don't need
@ConfigSerializable
data class BrewRecipe(
    val id: String,
    val name: String,
    val difficulty: Int,
    val cookingTime: Int,
    val distillRuns: Int,
    val distillTime: Int,
    val age: Int,
    val woodType: BarrelWoodType,
    val lore: List<String>,
    val ingredients: Map<String, Int>,
    val potionColor: Color?,
    val customModelData: Int,
    val rarityWeight: Int,
)

enum class BarrelWoodType {
    ANY,
    OAK,
    SPRUCE,
    BIRCH,
    JUNGLE,
    ACACIA,
    DARK_OAK,
    CRIMSON,
    WARPED,
    MANGROVE,
    CHERRY,
    BAMBOO,
    CHEST,
    TRAPPED_CHEST,
    BARREL,
    SMOKER,
    BLAST_FURNACE,
    CAMPFIRE,
}