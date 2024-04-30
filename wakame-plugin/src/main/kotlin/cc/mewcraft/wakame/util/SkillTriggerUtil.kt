package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.skill.SkillTrigger.Combo

object SkillTriggerUtil {
    fun List<SkillTrigger>.generateCombinations(combinationLength: Int): List<Combo> {
        val results = mutableListOf<Combo>()

        fun generate(currentCombo: List<SkillTrigger>) {
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