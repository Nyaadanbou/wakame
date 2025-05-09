package cc.mewcraft.wakame.integration.economy.intrinsics

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.integration.economy.EconomyIntegration
import cc.mewcraft.wakame.integration.economy.EconomyType
import org.bukkit.entity.Player
import java.util.*

/**
 * 一种以经验等级为货币单位的经济系统.
 * 实现要求玩家必须在线, 否则抛出异常.
 *
 * 计算方式: `1 等级` = `1 货币`.
 *
 * 仅用于开发与测试, 勿用于生产环境!
 */
internal object VanillaEconomyIntegration : EconomyIntegration {

    override val type: EconomyType = EconomyType.VANILLA

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

    private fun getPlayer(uuid: UUID): Player {
        return SERVER.getPlayer(uuid) ?: error("Player is not online")
    }

}