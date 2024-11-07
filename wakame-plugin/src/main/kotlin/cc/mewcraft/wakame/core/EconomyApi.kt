package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.Injector
import org.bukkit.Server
import org.bukkit.entity.Player
import org.koin.core.component.get
import java.util.UUID

/**
 * 最基本的经济系统接口.
 *
 * 主要用于跟外部的经济系统解耦, 也可以让项目代码更优雅的应对经济系统不存在的情况.
 */
interface EconomyApi {
    fun has(uuid: UUID, amount: Double): Result<Boolean>
    fun take(uuid: UUID, amount: Double): Result<Boolean>
    fun give(uuid: UUID, amount: Double): Result<Boolean>
}

/**
 * 一种以经验等级为货币单位的经济系统.
 * 实现要求玩家必须在线, 否则抛出异常.
 *
 * 计算方式: `1 等级` = `1 货币`.
 *
 * 仅用于开发与测试, 勿用于生产环境!
 */
data object LevelEconomyApi : EconomyApi {
    private fun getPlayer(uuid: UUID): Player {
        return Injector.get<Server>().getPlayer(uuid) ?: error("player is not online.")
    }

    override fun has(uuid: UUID, amount: Double): Result<Boolean> {
        val player = getPlayer(uuid)
        val value = player.level >= amount.toInt()
        return Result.success(value)
    }

    override fun take(uuid: UUID, amount: Double): Result<Boolean> {
        val player = getPlayer(uuid)
        if (player.level < amount.toInt()) {
            return Result.success(false)
        }

        player.level -= amount.toInt()
        return Result.success(true)
    }

    override fun give(uuid: UUID, amount: Double): Result<Boolean> {
        val player = getPlayer(uuid)
        player.level += amount.toInt()
        return Result.success(true)
    }

}