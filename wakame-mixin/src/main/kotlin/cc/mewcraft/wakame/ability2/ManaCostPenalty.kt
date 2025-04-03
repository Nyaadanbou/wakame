package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.util.cooldown.Cooldown

data class ManaCostPenalty(
    val resetCooldown: Cooldown = Cooldown.ofTicks(60),
    var penaltyCount: Int = 0
)