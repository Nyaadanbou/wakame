package cc.mewcraft.wakame.integration.economy.intrinsics

import cc.mewcraft.wakame.integration.economy.EconomyIntegration2
import cc.mewcraft.wakame.integration.economy.EconomyType
import org.bukkit.Bukkit
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
internal object VanillaEconomyIntegration : EconomyIntegration2 {

    override val type: EconomyType = EconomyType.VANILLA

    override val defaultCurrency: String? = null

    override fun has(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        val player = getPlayer(user)
        val value = player.level >= amount.toInt()
        return Result.success(value)
    }

    override fun hasAcc(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        return Result.failure(UnsupportedOperationException())
    }

    override fun take(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        val player = getPlayer(user)
        if (player.level < amount.toInt()) {
            return Result.success(false)
        }

        player.level -= amount.toInt()
        return Result.success(true)
    }

    override fun give(user: UUID, amount: Double, currency: String?): Result<Boolean> {
        val player = getPlayer(user)
        player.level += amount.toInt()
        return Result.success(true)
    }

    private fun getPlayer(uuid: UUID): Player {
        return Bukkit.getServer().getPlayer(uuid) ?: error("Player is not online")
    }
}