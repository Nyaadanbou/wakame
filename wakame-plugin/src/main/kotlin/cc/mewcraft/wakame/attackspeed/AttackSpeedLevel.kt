package cc.mewcraft.wakame.attackspeed

enum class AttackSpeedLevel(
    val cooldown: Int,
    val fatigueLevel: Int?
) {
    VERY_SLOW(50, 8),
    SLOW(40, 7),
    NORMAL(30, 5),
    FAST(20, 1),
    VERY_FAST(10, null)
}