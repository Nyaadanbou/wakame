package cc.mewcraft.wakame.enchantment2.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Component
import org.bukkit.inventory.FurnaceRecipe

class Smelter(
    val disableOnCrouch: Boolean,
    val smeltingSound: Identifier,
    val registeredFurnaceRecipes: HashSet<FurnaceRecipe>,
) : Component<Smelter> {

    companion object : EComponentType<Smelter>()

    override fun type() = Smelter

}