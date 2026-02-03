package cc.mewcraft.wakame.world

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.feature.FEATURE_CONFIG
import cc.mewcraft.wakame.util.cooldown.Cooldown
import cc.mewcraft.wakame.world.TimeControl.addTime
import cc.mewcraft.wakame.world.TimeControl.setTime
import net.kyori.adventure.util.Ticks
import org.bukkit.Bukkit
import xyz.xenondevs.commons.provider.map

/**
 * 控制所有维度的时间.
 */
object TimeControl {

    private val useInterval: Cooldown by FEATURE_CONFIG
        .entryOrElse<Long>(600L * Ticks.TICKS_PER_SECOND, "world_time_control_use_interval_ticks")
        .map(Cooldown::ofTicks)

    /**
     * 检查全局最小的使用间隔是否已经过去.
     * 如果返回 `true` 则可以继续执行 [setTime] 或 [addTime],
     * 否则应该提示玩家无法使用, 需要等待一段时间.
     */
    fun isReady(): Boolean {
        return useInterval.testSilently()
    }

    fun getTimeUntilReadyTicks(): Long {
        return useInterval.remainingMillis() / 50
    }

    fun setTime(time: Long) {
        for (world in Bukkit.getServer().worlds) {
            LOGGER.info("Setting time for all worlds: $time")
            var time2 = world.time
            time2 -= time2 % 24000
            world.time = time2 + 24000 + time
        }
        useInterval.reset()
    }

    fun addTime(time: Long) {
        for (world in Bukkit.getServer().worlds) {
            LOGGER.info("Adding time for all worlds: $time")
            var time2 = world.time
            world.time = time2 + time
        }
        useInterval.reset()
    }

    enum class ActionType {
        SET_TIME,
        ADD_TIME,
    }
}