package cc.mewcraft.wakame.integration.economy

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.integration.economy.intrinsics.VanillaEconomyIntegration
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.UUID

@PostWorldDependency(
    runBefore = [HooksLoader::class]
)
object EconomyManager : KoinComponent, Initializable {
    internal var integration: EconomyIntegration? = null

    init {
        // 初始化时, 将 LevelEconomyIntegration 作为默认的经济系统.
        // 如果有其他经济系统存在并且需要被使用, 该字段应该被重新赋值.
        integration = get<VanillaEconomyIntegration>()
    }

    /**
     * 检查玩家是否有足够的货币.
     */
    fun has(uuid: UUID, amount: Double): Result<Boolean> {
        return integration?.has(uuid, amount) ?: Result.failure(IllegalStateException("no economy integration found"))
    }

    /**
     * 从玩家身上扣除货币.
     */
    fun take(uuid: UUID, amount: Double): Result<Boolean> {
        return integration?.take(uuid, amount) ?: Result.failure(IllegalStateException("no economy integration found"))
    }

    /**
     * 给玩家增加货币.
     */
    fun give(uuid: UUID, amount: Double): Result<Boolean> {
        return integration?.give(uuid, amount) ?: Result.failure(IllegalStateException("no economy integration found"))
    }
}