package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.attribute.composite.*
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.cores.AttributeCore
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.decorate
import me.lucko.helper.text3.mini
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import kotlin.math.ceil

/**
 * 封装了一个标准的, 独立的, 重造流程.
 */
internal class MergeOperation
private constructor(
    private val session: MergingSession,
) : KoinComponent {

    companion object {
        operator fun invoke(session: MergingSession): MergingSession.Result {
            return MergeOperation(session).execute()
        }
    }

    private val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.MERGE)

    fun execute(): MergingSession.Result {
        if (session.frozen) {
            logger.error("Trying to merge a frozen merging session. This is a bug!")
            return ReforgeResult.failure("<gray>会话已冻结.".mini)
        }

        val inputItem1 = session.inputItem1
        val inputItem2 = session.inputItem2

        // 输入的物品不能是空
        if (inputItem1 == null || inputItem2 == null) {
            logger.info("Trying to merge with null input items.")
            return ReforgeResult.empty()
        }

        // 输入的物品必须是*便携式*属性*核心*
        val core1 = (inputItem1.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? AttributeCore)
            ?: return ReforgeResult.failure("<gray>第一个输入无法参与合并.".mini)
        val core2 = (inputItem2.components.get(ItemComponentTypes.PORTABLE_CORE)?.wrapped as? AttributeCore)
            ?: return ReforgeResult.failure("<gray>第二个输入无法参与合并.".mini)

        // 两个核心除了数值以外, 其余数据必须一致
        if (!core1.similarTo(core2)) {
            logger.info("Trying to merge cores with different attributes.")
            return ReforgeResult.failure("<gray>核心的种类不一致.".mini)
        }

        // 输入的核心种类至少要跟一条规则相匹配
        if (!session.table.acceptableCoreMatcher.test(core1) || !session.table.acceptableCoreMatcher.test(core2)) {
            logger.info("Trying to merge cores with unacceptable types.")
            return ReforgeResult.failure("<gray>核心无法在本台合并.".mini)
        }

        // 输入的物品等级必须低于工作台指定的值
        if (session.getLevel1() > session.table.maxInputItemLevel || session.getLevel2() > session.table.maxInputItemLevel) {
            logger.info("Trying to merge cores with too high level.")
            return ReforgeResult.failure("<gray>核心等级过高.".mini)
        }

        val attribute = core1.attribute

        val resultOp = attribute.operation
        val resultType = MergingTable.NumberMergeFunction.Type.by(resultOp)
        val resultValue = session.numberMergeFunction(resultType).evaluate()
        val resultCore = when (attribute /* 或者用 core2, 结果上没有区别 */) {
            is ConstantCompositeAttributeS -> {
                AttributeCore(id = core1.id, attribute = attribute.copy(value = resultValue))
            }

            is ConstantCompositeAttributeSE -> {
                AttributeCore(id = core1.id, attribute = attribute.copy(value = resultValue))
            }

            is ConstantCompositeAttributeR,
            is ConstantCompositeAttributeRE,
                -> {
                // 我们不支持拥有两个数值的核心, 原因:
                // - 实际的游戏设计中, 不太可能设计出合并这种核心
                // - 代码实现上, 每种组合都得考虑. 目前就有2*3=6种
                // - 拥有两个数值的核心也许本来就是个设计错误...
                logger.info("Trying to merge cores with multiple value structure.")
                return ReforgeResult.failure("<gray>核心无法合并.".mini)
            }
        }

        val resultPenalty = session.outputPenaltyFunction.evaluate().let(::ceil).toInt()

        // 合并后的惩罚值必须低于工作台指定的值
        if (resultPenalty > session.table.maxOutputItemPenalty) {
            logger.info("Trying to merge cores with too high penalty.")
            return ReforgeResult.failure("<gray>过于昂贵!".mini)
        }

        val resultLevel = session.outputLevelFunction.evaluate().let(::ceil).toInt()
        val resultRarity = run {
            // 选取权重较高的稀有度作为结果的稀有度
            val rarity1 = inputItem1.components.get(ItemComponentTypes.RARITY)?.rarity ?: RarityRegistry.DEFAULT
            val rarity2 = inputItem2.components.get(ItemComponentTypes.RARITY)?.rarity ?: RarityRegistry.DEFAULT
            maxOf(rarity1, rarity2)
        }

        // 输出的物品直接以 inputItem1 为基础进行修改
        val outputItem = inputItem1

        outputItem.components.set(ItemComponentTypes.LEVEL, ItemLevel(resultLevel))
        outputItem.components.set(ItemComponentTypes.RARITY, ItemRarity(resultRarity))
        outputItem.components.set(ItemComponentTypes.PORTABLE_CORE, PortableCore(resultCore, resultPenalty))

        val totalCost = session.currencyCostFunction.evaluate()

        return ReforgeResult.success(
            item = outputItem,
            type = ReforgeType.success(resultOp),
            cost = ReforgeCost.success(totalCost)
        )
    }
}