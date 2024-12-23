package cc.mewcraft.wakame.molang

import team.unnamed.mocha.runtime.binding.Binding

class ManaPenalty(
    val penaltyCount: Int
) {
    @Binding("count")
    fun count(): Int {
        return penaltyCount
    }
}