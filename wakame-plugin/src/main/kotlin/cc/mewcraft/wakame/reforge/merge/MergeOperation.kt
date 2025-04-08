package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.entity.attribute.bundle.*
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.ReforgeHistory
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.extension.level
import cc.mewcraft.wakame.item.extension.portableCore
import cc.mewcraft.wakame.item.extension.rarity
import cc.mewcraft.wakame.item.extension.reforgeHistory
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.decorate
import org.bukkit.entity.Player
import org.slf4j.Logger
import kotlin.math.ceil

/**
 * 封装了一个标准的, 独立的, 重造流程.
 */
internal class MergeOperation
private constructor(
    private val session: MergingSession,
) {

    companion object {
        operator fun invoke(session: MergingSession): MergingSession.ReforgeResult {
            return MergeOperation(session).execute()
        }
    }

    private val player: Player
        get() = session.viewer
    private val logger: Logger = LOGGER.decorate(prefix = ReforgingStationConstants.MERING_LOG_PREFIX)

    fun execute(): MergingSession.ReforgeResult {
        if (session.frozen) {
            logger.error("Trying to merge a frozen merging session. This is a bug!")
            return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_FROZEN_SESSION)
        }

        val inputItem1 = session.inputItem1
        val inputItem2 = session.inputItem2

        // 输入的物品不能是空
        if (inputItem1 == null || inputItem2 == null) {
            logger.info("Trying to merge with null input items.")
            return ReforgeResult.empty(player)
        }

        // 输入的物品必须是*便携式*属性*核心*
        val core1 = (inputItem1.portableCore?.wrapped as? AttributeCore)
            ?: return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_BAD_INPUT_1)
        val core2 = (inputItem2.portableCore?.wrapped as? AttributeCore)
            ?: return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_BAD_INPUT_2)

        // 两个核心除了数值以外, 其余数据必须一致
        if (!core1.similarTo(core2)) {
            logger.info("Trying to merge cores with different attributes.")
            return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_INPUTS_NOT_SIMILAR)
        }

        // 输入的核心种类至少要跟一条规则相匹配
        if (!session.table.acceptableCoreMatcher.test(core1) || !session.table.acceptableCoreMatcher.test(core2)) {
            logger.info("Trying to merge cores with unacceptable types.")
            return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_UNACCEPTABLE_TYPE)
        }

        // 输入的物品等级必须低于工作台指定的值
        if (session.getLevel1() > session.table.inputLevelLimit || session.getLevel2() > session.table.inputLevelLimit) {
            logger.info("Trying to merge cores with too high level.")
            return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_LEVEL_TOO_HIGH)
        }

        val resultedPenalty = session.outputPenaltyFunction.evaluate().let(::ceil).toInt()

        // 合并后的惩罚值必须低于工作台指定的值
        if (resultedPenalty > session.table.outputPenaltyLimit) {
            logger.info("Trying to merge cores with too high penalty.")
            return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_PENALTY_TOO_HIGH)
        }

        val attribute1 = core1.data
        val resultedOperation = attribute1.operation
        val (resultedValue, resultedScore) = session.valueMergeFunction(resultedOperation).evaluate()
        val resultedCore = when (attribute1 /* 或者用 core2, 结果上没有区别 */) {
            is ConstantAttributeBundleS -> AttributeCore(id = core1.id, data = attribute1.copy(value = resultedValue, quality = ConstantAttributeBundle.Quality.fromZScore(resultedScore)))
            is ConstantAttributeBundleSE -> AttributeCore(id = core1.id, data = attribute1.copy(value = resultedValue, quality = ConstantAttributeBundle.Quality.fromZScore(resultedScore)))
            is ConstantAttributeBundleR, is ConstantAttributeBundleRE -> {
                // 我们不支持拥有两个数值的核心, 原因:
                // - 实际的游戏设计中, 不太可能设计出合并这种核心
                // - 代码实现上, 每种组合都得考虑. 目前就有2*3=6种
                // - 拥有两个数值的核心也许本来就是个设计错误...
                logger.info("Trying to merge cores with multi-value structure.")
                return ReforgeResult.failure(player, TranslatableMessages.MSG_MERGING_RESULT_NON_MERGEABLE_TYPE)
            }
        }

        val resultedLevel = session.outputLevelFunction.evaluate().let(::ceil).toInt()
        val resultedRarity = run {
            // 选取权重较高的稀有度作为结果的稀有度
            val rarity1 = inputItem1.rarity
            val rarity2 = inputItem2.rarity
            maxOf(rarity1, rarity2, Comparator.comparing(RegistryEntry<Rarity>::unwrap))
        }

        // 输出的物品直接以 inputItem1 为基础进行修改
        val resultedItem = inputItem1.apply {
            level = resultedLevel
            rarity = resultedRarity
            portableCore = PortableCore(resultedCore)
            reforgeHistory = ReforgeHistory(resultedPenalty)
        }
        val totalCost = session.currencyCostFunction.evaluate()

        return ReforgeResult.success(
            player = player,
            item = resultedItem,
            type = ReforgeType.success(player, resultedOperation),
            cost = ReforgeCost.success(player, totalCost)
        )
    }
}