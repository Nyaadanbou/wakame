package cc.mewcraft.wakame.ability

import me.lucko.helper.cooldown.Cooldown

data class ManaCostPenalty(
    val cooldown: Cooldown = Cooldown.ofTicks(60),
    var penaltyCount: Int = 0
)