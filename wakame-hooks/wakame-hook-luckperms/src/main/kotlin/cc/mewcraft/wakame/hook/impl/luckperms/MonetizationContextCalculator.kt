package cc.mewcraft.wakame.hook.impl.luckperms

import cc.mewcraft.wakame.monetization.MonetizationCache
import net.luckperms.api.context.ContextCalculator
import net.luckperms.api.context.ContextConsumer
import net.luckperms.api.context.ContextSet
import net.luckperms.api.context.ImmutableContextSet
import org.bukkit.entity.Player
import java.math.BigDecimal

/**
 * LuckPerms 自定义 Context: 判断玩家的累积充值金额是否达到指定档位.
 *
 * 注册后, 可在 LuckPerms 权限条件中使用, 例如:
 * ```
 * lp user <player> permission set <perm> true paid-above-60=true
 * ```
 *
 * 档位由配置文件 `monetization.yml` 中的 `luckperms_integration.thresholds` 驱动.
 */
internal class MonetizationContextCalculator(
    thresholds: List<Int>,
) : ContextCalculator<Player> {

    companion object {
        private fun keyFor(threshold: Int) = "paid-above-$threshold"
    }

    private val thresholds: IntArray = thresholds.toIntArray()

    override fun calculate(target: Player, consumer: ContextConsumer) {
        val totalPaid = MonetizationCache.getTotalPaidAmount(target.uniqueId)
        val amount = totalPaid.toBigDecimalOrNull() ?: BigDecimal.ZERO
        for (threshold in thresholds) {
            consumer.accept(keyFor(threshold), (amount >= BigDecimal(threshold)).toString())
        }
    }

    override fun estimatePotentialContexts(): ContextSet {
        val builder = ImmutableContextSet.builder()
        for (threshold in thresholds) {
            builder.add(keyFor(threshold), "true")
            builder.add(keyFor(threshold), "false")
        }
        return builder.build()
    }
}
