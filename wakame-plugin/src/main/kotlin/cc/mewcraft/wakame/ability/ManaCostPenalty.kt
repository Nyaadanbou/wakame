package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.util.cooldown.Cooldown

data class ManaCostPenalty(
    val cooldown: Cooldown = Cooldown.ofTicks(60),
    var penaltyCount: Int = 0
)