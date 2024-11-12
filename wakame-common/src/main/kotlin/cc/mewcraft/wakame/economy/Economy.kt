package cc.mewcraft.wakame.economy

import java.util.UUID

/**
 * 最基本的经济系统接口.
 *
 * 主要用于跟外部的经济系统解耦, 也可以让项目代码更优雅的应对经济系统不存在的情况.
 */
interface Economy {
    fun has(uuid: UUID, amount: Double): Result<Boolean>
    fun take(uuid: UUID, amount: Double): Result<Boolean>
    fun give(uuid: UUID, amount: Double): Result<Boolean>
}