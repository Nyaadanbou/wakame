package cc.mewcraft.wakame.enchantment2.effect

import io.papermc.paper.math.Position
import org.bukkit.World
import org.bukkit.entity.Entity

interface EnchantmentEntityEffect : EnchantmentLocationBasedEffect {

    fun apply(
        world: World,
        level: Int,
        context: EnchantmentEffectContext,
        player: Entity,
        position: Position,
    )

    override fun apply(
        world: World,
        level: Int,
        context: EnchantmentEffectContext,
        player: Entity,
        position: Position,
        newApplied: Boolean,
    ) {
        apply(world, level, context, player, position)
    }

}