package cc.mewcraft.wakame.enchantment.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.util.KoishKey
import com.github.quillraven.fleks.Component
import org.bukkit.inventory.FurnaceRecipe

class Smelter(
    val disableOnCrouch: Boolean,
    val smeltingSound: KoishKey,
    val registeredFurnaceRecipes: HashSet<FurnaceRecipe>,
) : Component<Smelter> {

    companion object : EComponentType<Smelter>()

    override fun type() = Smelter

}