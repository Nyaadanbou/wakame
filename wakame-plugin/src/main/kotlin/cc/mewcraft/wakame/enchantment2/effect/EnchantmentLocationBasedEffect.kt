package cc.mewcraft.wakame.enchantment2.effect

import io.papermc.paper.math.Position
import org.bukkit.World
import org.bukkit.entity.Entity

interface EnchantmentLocationBasedEffect {

    fun apply(
        world: World,
        level: Int,
        context: EnchantmentEffectContext,
        player: Entity,
        position: Position,
        newApplied: Boolean,
    )

    fun remove(
        context: EnchantmentEffectContext,
        player: Entity,
        position: Position,
        level: Int,
    ) = Unit

}