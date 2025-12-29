package cc.mewcraft.bettergui.requirement

import cc.mewcraft.wakame.integration.economy.EconomyIntegration2
import me.hsgamer.bettergui.BetterGUI
import me.hsgamer.bettergui.api.requirement.Requirement
import me.hsgamer.bettergui.api.requirement.TakableRequirement
import me.hsgamer.bettergui.builder.RequirementBuilder
import me.hsgamer.bettergui.config.MessageConfig
import me.hsgamer.bettergui.util.StringReplacerApplier
import me.hsgamer.hscore.bukkit.utils.MessageUtils
import me.hsgamer.hscore.common.StringReplacer
import me.hsgamer.hscore.common.Validate
import org.bukkit.Bukkit
import java.util.*


/**
 * 检查指定货币的余额(可同时扣除).
 *
 * 格式:
 *
 * ```yaml
 * balance:
 *   value: "1000;R"
 *   take: false
 * ```
 */
class BalanceReq : TakableRequirement<Pair<String, Double>> {

    constructor(input: RequirementBuilder.Input) : super(input) {
        menu.variableManager.register(name, StringReplacer.of { original, uuid ->
            val final = getFinalValue(uuid) // Pair(currency, amount)
            val currency = final.first
            val amount = final.second
            if (amount > 0 && !EconomyIntegration2.has(uuid, amount, currency).getOrDefault(false)) {
                return@of amount.toString()
            }
            return@of BetterGUI.getInstance().get(MessageConfig::class.java).haveMetRequirementPlaceholder
        }, true)
    }

    override fun getDefaultTake(): Boolean {
        return true
    }

    override fun getDefaultValue(): Any {
        return "0;R"
    }

    override fun convert(value: Any, uuid: UUID): Pair<String, Double> {
        // 先做占位符替换
        val replaced = StringReplacerApplier.replace(value.toString().trim(), uuid, this)

        // 支持 "<amount>;<currency>" 或 "<amount>"
        val parts = replaced.split(";", limit = 2)
        val amountPart = parts.getOrNull(0)?.trim().orEmpty()
        val currencyPart = parts.getOrNull(1)?.trim().takeUnless { it.isNullOrEmpty() } ?: "R"

        val amount = Validate.getNumber(amountPart)
            .map { it.toDouble() }
            .orElseGet {
                MessageUtils.sendMessage(uuid, BetterGUI.getInstance().get(MessageConfig::class.java).getInvalidNumber(replaced))
                0.0
            }

        return currencyPart to amount
    }

    override fun checkConverted(uuid: UUID, value: Pair<String, Double>): Requirement.Result {
        val player = Bukkit.getPlayer(uuid)
        if (player == null) {
            // 与其他 requirement 一致: 离线不限制
            return Requirement.Result.success()
        }

        val (currency, amount) = value

        if (amount <= 0.0) {
            // 不需要钱或配置为 0, 直接通过且不扣钱
            return Requirement.Result.success()
        }

        if (!EconomyIntegration2.has(uuid, amount, currency).getOrDefault(false)) {
            // 余额不足
            return Requirement.Result.fail()
        }

        // 余额足够, 视是否需要扣除
        return successConditional sc@{ uuid1 ->
            // 再次检测以防竞态
            if (EconomyIntegration2.has(uuid1, amount, currency).getOrDefault(false)) {
                EconomyIntegration2.take(uuid1, amount, currency)
            }
        }
    }
}