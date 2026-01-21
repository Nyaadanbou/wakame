package cc.mewcraft.bettergui.action

import cc.mewcraft.wakame.integration.economy.EconomyIntegration2
import me.hsgamer.bettergui.builder.ActionBuilder
import me.hsgamer.bettergui.util.SchedulerUtil
import me.hsgamer.hscore.action.common.Action
import me.hsgamer.hscore.common.StringReplacer
import me.hsgamer.hscore.common.Validate
import me.hsgamer.hscore.task.element.TaskProcess
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*
import kotlin.jvm.optionals.getOrNull


/**
 * 添加指定类型的货币余额.
 *
 * 格式:
 *
 * ```yaml
 * give-balance(<currency>): <amount>
 * ```
 */
class GiveBalance : Action {

    private val amount: String
    private val currency: String

    constructor(input: ActionBuilder.Input) {
        this.amount = input.value
        this.currency = input.optionAsList[0]
    }

    override fun apply(uuid: UUID, process: TaskProcess, stringReplacer: StringReplacer) {
        val amount2 = stringReplacer.replaceOrOriginal(this.amount, uuid)
        val currency2 = stringReplacer.replaceOrOriginal(this.currency, uuid)
        val player = Bukkit.getPlayer(uuid) ?: run {
            process.next()
            return
        }
        val amount = Validate.getNumber(amount2).map(BigDecimal::toDouble).getOrNull() ?: run {
            player.sendRichMessage("<red>Invalid currency amount: $amount2")
            process.next()
            return
        }
        if (amount > 0) {
            SchedulerUtil.entity(player).run({
                try {
                    EconomyIntegration2.give(uuid, amount, currency2).onFailure {
                        player.sendRichMessage("<red>Error: the transaction couldn't be executed.")
                    }
                } finally {
                    process.next()
                }
            }, process::next)
        } else {
            process.next()
        }
    }
}