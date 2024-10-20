package cc.mewcraft.wakame.player.attackspeed

enum class AttackSpeedLevel(
    /**
     * 攻击冷却时长, 单位: tick.
     */
    val cooldown: Int,
) {
    VERY_SLOW(30),
    SLOW(25),
    NORMAL(20),
    FAST(15),
    VERY_FAST(10)
}