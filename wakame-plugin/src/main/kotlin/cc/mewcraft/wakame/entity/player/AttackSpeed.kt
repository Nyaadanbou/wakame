package cc.mewcraft.wakame.entity.player

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * 本函数用于构建一个 [AttackSpeed] 实例.
 */
fun AttackSpeed(player: Player): AttackSpeed {
    return PlayerAttackSpeed(player.uniqueId)
}

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

/**
 * 封装了一名玩家的所有攻击冷却状态.
 */
sealed interface AttackSpeed {
    /**
     * “激活”指定的冷却. 如果已存在将覆盖原有的冷却状态.
     */
    fun activate(key: Key, level: AttackSpeedLevel)

    /**
     * 查询指定的冷却是否为“激活”状态.
     */
    fun isActive(key: Key): Boolean

    /**
     * 查询离冷却变为“未激活”状态的剩余时间, 单位: tick.
     */
    fun getRemainingTicks(key: Key): Int

    /**
     * 重置指定冷却的状态, 使其变为“未激活”.
     */
    fun reset(key: Key)
}

private class PlayerAttackSpeed(
    uniqueId: UUID,
) : AttackSpeed {
    /**
     * 记录了每个冷却变为“未激活”的时间戳.
     * - K: 冷却的索引
     * - V: 冷却变为"未激活"的游戏刻
     */
    private val inactiveTimestamps = Object2IntOpenHashMap<Key>()

    override fun activate(key: Key, level: AttackSpeedLevel) {
        inactiveTimestamps[key] = Bukkit.getServer().currentTick + level.cooldown
    }

    override fun isActive(key: Key): Boolean {
        return inactiveTimestamps.getInt(key) > Bukkit.getServer().currentTick
    }

    override fun getRemainingTicks(key: Key): Int {
        val diff = inactiveTimestamps.getInt(key) - Bukkit.getServer().currentTick
        return if (diff < 0) 0 else diff
    }

    override fun reset(key: Key) {
        inactiveTimestamps.put(key, 0)
    }
}
