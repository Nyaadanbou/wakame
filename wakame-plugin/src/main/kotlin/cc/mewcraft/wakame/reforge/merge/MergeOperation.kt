package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemLevel
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeS
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeSE
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import me.lucko.helper.text3.mini
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.math.ceil

/**
 * 封装了一个标准的, 独立的, 重造流程.
 */
internal class MergeOperation(
    private val session: MergingSession,
) : KoinComponent {

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.MERGE
    }

    private val logger: Logger by inject()

    fun execute(): MergingSession.Result {
        if (session.frozen) {
            logger.error("$PREFIX Trying to merge a frozen merging session. This is a bug!")
            return Result.failure("<gray>会话已被冻结.".mini)
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
            ?: return Result.failure("<gray>第一个物品不是属性类核心.".mini)
        val core2 = (inputItem2.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? CoreAttribute)
            ?: return Result.failure("<gray>第二个物品不是属性类核心.".mini)

        // 两个核心除了数值以外, 其余数据必须一致
        if (!core1.isSimilar(core2)) {
            logger.info("$PREFIX Trying to merge cores with different attributes.")
            return Result.failure("<gray>两个核心的属性种类不一致.".mini)
        }

        // 输入的核心种类至少要跟一条规则相匹配
        if (!session.table.acceptedCoreMatcher.test(core1) || !session.table.acceptedCoreMatcher.test(core2)) {
            logger.info("$PREFIX Trying to merge cores with unacceptable types.")
            return Result.failure("<gray>核心无法在本合并台进行合并.".mini)
        }

        // 输入的物品等级必须低于工作台指定的值
        if (session.getLevel1() > session.table.maxInputItemLevel || session.getLevel2() > session.table.maxInputItemLevel) {
            logger.info("$PREFIX Trying to merge cores with too high level.")
            return Result.failure("<gray>核心的等级超出了本合并台的上限.".mini)
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
                return Result.failure("<gray>核心的类型无法参与合并.".mini)
            }
        }

        val mergedPenalty = session.outputPenaltyFunction.evaluate().let(::ceil).toInt()

        // 合并后的惩罚值必须低于工作台指定的值
        if (mergedPenalty > session.table.maxOutputItemPenalty) {
            logger.info("$PREFIX Trying to merge cores with too high penalty.")
            return Result.failure("<gray>过于昂贵!".mini)
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