package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeS
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeSE
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.merge.SimpleMergingSession.Cost
import cc.mewcraft.wakame.reforge.merge.SimpleMergingSession.Result
import cc.mewcraft.wakame.reforge.merge.SimpleMergingSession.Type
import org.slf4j.Logger
import kotlin.math.ceil

/**
 * 封装了一个标准的, 独立的, 重造流程.
 */
// 构造器必须是 exception-free !!!
internal class MergeOperation(
    private val session: MergingSession,
    private val logger: Logger,
) {
    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.MERGE
    }

    fun execute(): MergingSession.Result {
        if (session.frozen) {
            logger.error("$PREFIX Trying to merge a frozen merging session. This is a bug!")
            return Result.failure()
        }

        val inputItem1 = session.inputItem1
        val inputItem2 = session.inputItem2

        // 输入的物品不能是空
        if (inputItem1 == null || inputItem2 == null) {
            logger.info("$PREFIX Trying to merge with null input items.")
            return Result.empty()
        }

        // 输入的物品必须是*便携式*属性*核心*
        val core1 = (inputItem1.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? CoreAttribute)
            ?: return Result.failure()
        val core2 = (inputItem2.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? CoreAttribute)
            ?: return Result.failure()

        // 两个核心除了数值以外, 其余数据必须一致
        if (!core1.isSimilar(core2)) {
            logger.info("$PREFIX Trying to merge cores with different attributes.")
            return Result.failure()
        }

        // 输入的核心种类至少要跟一条规则相匹配
        if (!session.table.acceptedCoreMatcher.test(core1) || !session.table.acceptedCoreMatcher.test(core2)) {
            logger.info("$PREFIX Trying to merge cores with unacceptable types.")
            return Result.failure()
        }

        // 输入的物品等级必须低于工作台指定的值
        if (session.getLevel1() > session.table.maxInputItemLevel || session.getLevel2() > session.table.maxInputItemLevel) {
            logger.info("$PREFIX Trying to merge cores with too high level.")
            return Result.failure()
        }

        val mergedOp = core1.operation
        val mergeType = MergingTable.NumberMergeFunction.Type.by(mergedOp)
        val mergedValue = session.numberMergeFunction(mergeType).evaluate()
        val mergedCore = when (core1 /* 或者用 core2, 结果上没有区别 */) {
            is CoreAttributeS -> core1.copy(value = mergedValue)
            is CoreAttributeSE -> core1.copy(value = mergedValue)

            // 我们不支持拥有两个数值的核心, 原因:
            // - 实际的游戏设计中, 不太可能设计出合并这种核心
            // - 代码实现上, 每种组合都得考虑. 目前就有2*3=6种
            // - 拥有两个数值的核心也许本来就是个设计错误...

            else -> {
                logger.info("$PREFIX Trying to merge cores with multiple value structure.")
                return Result.failure()
            }
        }

        val mergedPenalty = session.outputPenaltyFunction.evaluate().let(::ceil).toInt()

        // 合并后的惩罚值必须低于工作台指定的值
        if (mergedPenalty > session.table.maxOutputItemPenalty) {
            logger.info("$PREFIX Trying to merge cores with too high penalty.")
            return Result.failure()
        }

        val mergedLevel = session.outputLevelFunction.evaluate().let(::ceil).toInt().toShort()

        // 输出的物品直接以 inputItem1 为基础进行修改
        val outputItem: NekoStack = inputItem1
        outputItem.components.set(ItemComponentTypes.LEVEL, ItemLevel(mergedLevel))
        outputItem.components.set(ItemComponentTypes.PORTABLE_CORE, PortableCore(mergedCore, mergedPenalty))

        val totalCost = session.currencyCostFunction.evaluate()

        return Result.success(
            item = outputItem,
            type = Type.success(mergedOp),
            cost = Cost.success(totalCost)
        )
    }
}