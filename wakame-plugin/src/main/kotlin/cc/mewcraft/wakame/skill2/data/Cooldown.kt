package cc.mewcraft.wakame.skill2.data

data class Cooldown(
    var timeout: Float,
    var elapsed: Float = 0f
) {
    private val originTimeout: Float = timeout

    /**
     * 获取冷却的剩余时间.
     */
    val cooldownTime: Float
        get() = timeout - elapsed

    /**
     * 返回是否处于冷却当中.
     *
     * @return true 代表当前并未处于冷却中.
     */
    fun testCooldown(): Boolean {
        return elapsed >= timeout
    }

    /**
     * 重置冷却阈值为初始状态.
     */
    fun resetTimeout() {
        timeout = originTimeout
    }
}
