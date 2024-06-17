package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import team.unnamed.mocha.runtime.binding.Binding

class SkillCastContextQuery(
    private val context: SkillCastContext
) {
    @Binding("is_player")
    fun isPlayer(): Boolean {
        return context.has(SkillCastContextKey.CASTER_PLAYER)
    }
}