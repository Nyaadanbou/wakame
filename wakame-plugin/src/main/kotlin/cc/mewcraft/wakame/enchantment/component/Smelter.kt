package cc.mewcraft.wakame.enchantment.component

import cc.mewcraft.wakame.util.KoishKey
import org.bukkit.inventory.FurnaceRecipe

class Smelter(
    val disableOnCrouch: Boolean,
    val smeltingSound: KoishKey,
    val registeredFurnaceRecipes: HashSet<FurnaceRecipe>,
)