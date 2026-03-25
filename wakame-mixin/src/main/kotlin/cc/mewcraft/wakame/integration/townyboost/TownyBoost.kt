package cc.mewcraft.wakame.integration.townyboost

import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

/**
 * 城镇权益系统的接口.
 *
 * 米团玩家可以使用"城镇权益激活卡", 将自己的米团等级对应的权益施加到所在城镇上.
 * 权益包括: 领地上限加成, 圈地花费修饰, 城镇/国家维护费修饰.
 *
 * 在一个位面内, 每名玩家最多将权益施加到一个城镇上; 不同位面互不影响.
 */
interface TownyBoost {

    /**
     * 在玩家当前位置的城镇上激活权益.
     *
     * 如果玩家在同一位面的另一个城镇已有权益, 会先移除旧权益.
     */
    fun activate(player: Player): ActivateResult

    /**
     * 移除玩家在指定位面中已激活的城镇权益.
     *
     * @return `true` 如果成功移除, `false` 如果玩家在该位面没有已激活的权益
     */
    fun deactivate(playerId: UUID, world: World): Boolean

    /**
     * 激活结果.
     */
    enum class ActivateResult {
        /** 成功激活. */
        SUCCESS,
        /** 玩家不在任何城镇的领地内. */
        NOT_IN_TOWN,
        /** 玩家没有任何匹配的权限组. */
        NO_VIP_GROUP,
    }

    companion object Impl : TownyBoost {

        private var implementation: TownyBoost = object : TownyBoost {
            override fun activate(player: Player): ActivateResult = ActivateResult.NOT_IN_TOWN
            override fun deactivate(playerId: UUID, world: World): Boolean = false
        }

        fun setImplementation(provider: TownyBoost) {
            implementation = provider
        }

        override fun activate(player: Player): ActivateResult = implementation.activate(player)
        override fun deactivate(playerId: UUID, world: World): Boolean = implementation.deactivate(playerId, world)
    }
}