package cc.mewcraft.wakame.enchantment2.effect

import io.papermc.paper.math.Position
import org.bukkit.World
import org.bukkit.entity.Entity
import kotlin.random.Random

interface AllOfEnchantmentEffects {

    companion object {
        fun allOf(vararg entityEffects: EnchantmentEntityEffect): EntityEffects {
            return EntityEffects(entityEffects.toList())
        }

        fun allOf(vararg locationBasedEffects: EntityEffects): LocationBasedEffects {
            return LocationBasedEffects(locationBasedEffects.toList())
        }

        fun allOf(vararg valueEffects: EnchantmentValueEffect): ValueEffects {
            return ValueEffects(valueEffects.toList())
        }
    }

    data class EntityEffects(val effects: List<EnchantmentEntityEffect>) : EnchantmentEntityEffect {
        override fun apply(
            world: World,
            level: Int,
            context: EnchantmentEffectContext,
            player: Entity,
            position: Position,
        ) {
            for (effect in effects) {
                effect.apply(world, level, context, player, position)
            }
        }
    }

    data class LocationBasedEffects(val effects: List<EnchantmentLocationBasedEffect>) : EnchantmentLocationBasedEffect {
        override fun apply(world: World, level: Int, context: EnchantmentEffectContext, player: Entity, position: Position, newApplied: Boolean) {
            for (effect in effects) {
                effect.apply(world, level, context, player, position, newApplied)
            }
        }

        override fun remove(context: EnchantmentEffectContext, player: Entity, position: Position, level: Int) {
            for (effect in effects) {
                effect.remove(context, player, position, level)
            }
        }
    }

    data class ValueEffects(val effects: List<EnchantmentValueEffect>) : EnchantmentValueEffect {
        override fun apply(level: Int, random: Random, inputValue: Float): Float {
            var inputValue2 = inputValue
            for (effect in effects) {
                inputValue2 = effect.apply(level, random, inputValue)
            }
            return inputValue2
        }
    }

}