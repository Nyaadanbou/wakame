package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeS
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeSE
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
    fun execute(): MergingSession.Result {
        if (session.frozen) {
            logger.error("Trying to merge a frozen merging session. This is a bug!")
            return Result.failure()
        }

        val inputItemX = session.inputItem1
        val inputItemY = session.inputItem2

        if (inputItemX == null || inputItemY == null) {
            logger.info("Trying to merge with null input items.")
            return Result.empty()
        }

        val coreX = (inputItemX.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? CoreAttribute) ?: return Result.failure()
        val coreY = (inputItemY.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? CoreAttribute) ?: return Result.failure()

        if (!coreX.isSimilar(coreY)) {
            logger.info("Trying to merge cores with different attributes.")
            return Result.failure()
        }

        val mergedOperation = coreX.operation
        val mergeType = MergingTable.NumberMergeFunction.Type.by(mergedOperation)
        val mergedValue = session.numberMergeFunction(mergeType).evaluate()
        val mergedCore = when (coreX) {
            is CoreAttributeS -> {
                coreX.copy(value = mergedValue)
            }

            is CoreAttributeSE -> {
                coreX.copy(value = mergedValue)
            }

            // 我们不支持拥有两个数值的核心, 原因:
            // - 实际的游戏设计中, 不太可能设计出合并这种核心
            // - 代码实现上, 每种组合都得考虑. 目前就有2*3=6种
            // - 拥有两个数值的核心也许本来就是个设计错误...

            else -> {
                logger.error("Can't merge the cores: $coreX, $coreY")
                return Result.failure()
            }
        }

        val mergedPenalty = session.outputPenaltyFunction.evaluate().let(::ceil).toInt()
        val mergedLevel = session.outputLevelFunction.evaluate().let(::ceil).toInt().toShort()
        val totalCost = session.currencyCostFunction.evaluate()

        inputItemX.components.set(ItemComponentTypes.LEVEL, ItemLevel(mergedLevel))
        inputItemX.components.set(ItemComponentTypes.PORTABLE_CORE, PortableCore(mergedCore, mergedPenalty))

        return Result.success(
            item = inputItemX,
            type = Type.success(mergedOperation),
            cost = Cost.success(totalCost)
        )
    }
}