package cc.mewcraft.wakame.player.attackspeed

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
    VERY_SLOW(30, 7),
    SLOW(25, 5),
    NORMAL(20, 3),
    FAST(15, 1),
    VERY_FAST(10, null)
}