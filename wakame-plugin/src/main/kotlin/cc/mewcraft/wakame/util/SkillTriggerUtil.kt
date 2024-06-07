package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.Trigger.Combo

object SkillTriggerUtil {
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
}