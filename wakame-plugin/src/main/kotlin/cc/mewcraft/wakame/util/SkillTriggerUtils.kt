package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.Trigger.Combo

fun List<Trigger>.generateCombinations(combinationLength: Int): List<Combo> {
    val results = mutableListOf<Combo>()

    fun generate(currentCombo: List<Trigger>) {
        if (currentCombo.size == combinationLength) {
            results.add(Combo(currentCombo))
            return
        }

        for (i in indices) {
            generate(currentCombo + this[i])
        }
    }

    generate(emptyList())
    return results
}

fun Iterable<Trigger>.toCombo(count: Int = 3): Combo {
    val iterator = this.iterator()
    val triggers = mutableListOf<Trigger>()
    var counter = 0
    while (iterator.hasNext() && counter < count) {
        triggers.add(iterator.next())
        counter++
    }
    return Combo(triggers)
}

fun Iterable<Trigger>.hasCombo(): Boolean {
    return this.any { it is Combo }
}