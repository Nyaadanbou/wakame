package cc.mewcraft.wakame.enchantment2.effect

import kotlin.random.Random

interface EnchantmentValueEffect {

    fun apply(level: Int, random: Random, inputValue: Float): Float

}