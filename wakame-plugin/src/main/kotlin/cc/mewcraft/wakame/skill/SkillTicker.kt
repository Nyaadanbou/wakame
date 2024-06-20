package cc.mewcraft.wakame.skill

import it.unimi.dsi.fastutil.objects.ObjectArrayList

/**
 * 用于处理非玩家状态的 [SkillTick]
 */
object SkillTicker {
    private val children: MutableList<SkillTick> = ObjectArrayList()

    fun tick() {
        for (child in children) {
            val result = child.tick()
            if (result == TickResult.ALL_DONE) {
                children.remove(child)
            }
        }
    }

    fun addChildren(skillTick: SkillTick) {
        children.add(skillTick)
    }

    fun getChildren(): List<SkillTick> {
        return children
    }
}