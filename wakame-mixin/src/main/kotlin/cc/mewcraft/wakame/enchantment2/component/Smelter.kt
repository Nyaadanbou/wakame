package cc.mewcraft.wakame.enchantment2.component

import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.inventory.FurnaceRecipe

class Smelter(
    val disableOnCrouch: Boolean,
    val smeltingSound: Identifier,
    val registeredFurnaceRecipes: HashSet<FurnaceRecipe>,
) : Component<Smelter> {

    companion object : ComponentType<Smelter>()

    override fun type() = Smelter

}