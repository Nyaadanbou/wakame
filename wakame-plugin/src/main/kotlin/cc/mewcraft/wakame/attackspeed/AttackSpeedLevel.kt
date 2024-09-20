package cc.mewcraft.wakame.attackspeed

enum class AttackSpeedLevel(
    /**
     * 攻击冷却时长, 单位: tick.
     */
    val cooldown: Int,
    /**
     * 攻击疲劳等级, 参考 [org.bukkit.potion.PotionEffect.amplifier].
     */
    val fatigueLevel: Int?
) {
    VERY_SLOW(50, 8),
    SLOW(40, 7),
    NORMAL(30, 5),
    FAST(20, 1),
    VERY_FAST(10, null)
}