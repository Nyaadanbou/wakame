package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill
import cc.mewcraft.wakame.item.components.cells.template.cores.empty.TemplateCoreEmpty
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationTrigger
import cc.mewcraft.wakame.item.templates.filter.AttributeContextHolder
import cc.mewcraft.wakame.item.templates.filter.SkillContextHolder
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.registry.RarityRegistry
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.slf4j.Logger
import kotlin.properties.Delegates

/**
 * 封装了一个标准的, 独立的, 重造流程.
 *
 * 该类不应该修改下面这些对象的状态:
 * - [RerollingSession]
 * - [RerollingSession.Selection]
 * - [RerollingSession.SelectionMap]
 */
internal class ReforgeOperation
/**
 * @throws ReforgeOperationException 如果物品缺少必要的组件
 */
constructor(
    private val session: RerollingSession,
    private val logger: Logger,
) : KoinComponent {

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.REROLL
    }

    /**
     * 要被重造的物品; 我们将对该实例进行修改.
     */
    private val inputItem: NekoStack = session.inputItem // it's a clone

    /**
     * 要被重造的物品的信息.
     */
    private val inputItemInfo: InputItemInfo = InputItemInfo(inputItem)

    /**
     * 该对象是否已被冻结. 被冻结的对象无法再执行函数 [execute].
     */
    private var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("$PREFIX Unfreezing an operation is prohibited. This is a bug!")
            return@vetoable false
        }
        logger.info("$PREFIX Operation frozen status updated: $old -> $new")
        return@vetoable true
    }

    private fun createMissingComponentException(type: ItemComponentType<*>): Nothing {
        throw ReforgeOperationException("$PREFIX Input item has no '$type' component. This is a bug!")
    }

    /**
     * 基于要被重造的物品 ([inputItemInfo]), 初始化上下文.
     *
     * 该上下文将被用于生成新的词条栏.
     */
    private fun generateContext(): Result<GenerationContext> {
        // 创建一个空的 context
        val trigger = GenerationTrigger.direct(inputItemInfo.level)
        val context = GenerationContext(trigger, inputItemInfo.key)

        // 先把*不可由玩家改变的信息*全部写入上下文
        context.level = inputItemInfo.level.toShort()
        context.rarity = inputItemInfo.rarity
        context.elements += inputItemInfo.elements
        context.kizamiz += inputItemInfo.kizamiz

        val selections = session.selections

        // 然后再把*可由玩家改变的信息*全部写入上下文
        inputItemInfo.cells
            .filterx { cell ->
                // 注意, 我们必须跳过玩家选择要重造的词条栏.
                // 如果不跳过, 那么新的词条栏将无法被正确生成.
                // 例如, 由于已存在相同的信息而最终生成了空词条栏.
                selections[cell.getId()]?.selected == false
            }
            .forEach { (_, cell) ->
                when (
                    val core = cell.getCore()
                ) {
                    is CoreSkill -> {
                        context.skills += SkillContextHolder(core.key)
                    }

                    is CoreAttribute -> {
                        context.attributes += AttributeContextHolder(core.key, core.operation, core.element)
                    }
                }
            }

        return Result.success(context)
    }

    /**
     * 执行一次重造.
     *
     * 基本流程:
     * - 计算最终的重造花费
     * - 重新生成选择的核心
     * - 更新词条栏的重造次数
     *
     * @throws ReforgeOperationException 如果重造失败.
     */
    fun execute(): RerollingSession.Result {
        if (frozen) {
            logger.error("$PREFIX Trying to execute a frozen operation. This is a bug!")
            return SimpleRerollingSession.Result.error()
        }

        frozen = true

        // 将要作为输出的物品.
        val outputItem = inputItem.clone()

        // 准备上下文, 用于重新生成核心.
        val context = generateContext().getOrElse {
            logger.error("$PREFIX An internal error occurred while generating context: ${it.message}")
            return SimpleRerollingSession.Result.error()
        }

        // 储存词条栏的总花费 (选择/未选择).
        var sumOfEachSelected = .0
        var sumOfEachUnselected = .0

        val table = session.table
        val selections = session.selections

        // 词条栏 (builder)
        val cells = inputItemInfo.cells.builder()

        // 遍历每一个选择:
        for ((id, sel) in selections) {

            // 计算当前词条栏的花费.
            val eachCost = table.currencyCost.eachFunction.compute(
                cost = sel.rule.cost,
                maxReroll = sel.rule.maxReroll,
                rerollCount = cells.get(id)?.getReforgeHistory()?.rerollCount ?: 0
            )
            // 分别加到选择/未选择的总花费上.
            if (sel.selected) {
                sumOfEachSelected += eachCost
            } else {
                sumOfEachUnselected += eachCost
            }

            // 如果玩家选择了该词条栏:
            if (sel.selected) {

                // 重新生成选择的核心 (这里跟从模板生成物品时的逻辑一样)
                cells.modify(id) {
                    val selected = sel.template.select(context).firstOrNull() ?: TemplateCoreEmpty
                    val generated = selected.generate(context)
                    it.setCore(generated)
                }

                // 为重造过的词条栏 +1 重造次数
                cells.modify(id) {
                    val oldReforgeHistory = it.getReforgeHistory()
                    val newReforgeHistory = oldReforgeHistory.addRerollCount(1)
                    it.setReforgeHistory(newReforgeHistory)
                }
            }
        }

        // 将新的词条栏组件写入物品.
        outputItem.components.set(ItemComponentTypes.CELLS, cells.build())

        // 计算重造物品的总花费.
        val compute = table.currencyCost.totalFunction.compute(
            base = table.currencyCost.base,
            rarity = table.rarityNumberMapping.get(inputItemInfo.rarity.key),
            itemLevel = inputItemInfo.level,
            allCount = selections.size,
            selectedCount = selections.count { it.selected },
            selectedCostSum = sumOfEachSelected,
            unselectedCostSum = sumOfEachUnselected,
        )
        val total = SimpleRerollingSession.Total.success(compute)

        return SimpleRerollingSession.Result.success(total, outputItem)
    }
}


/**
 * 封装了重造过程中 [RerollingSession.inputItem] 的必要信息.
 */
private class InputItemInfo
/**
 * @throws ReforgeOperationException 如果 [wrapped] 缺少必要的组件.
 */
constructor(
    wrapped: NekoStack,
) {
    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.REROLL
    }

    val key: Key = wrapped.key

    val level: Int
    val cells: ItemCells
    val rarity: Rarity
    val elements: Set<Element>
    val kizamiz: Set<Kizami>

    init {
        // 等级和词条栏是必要的
        level = wrapped.components.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: throwMissingComponentException(ItemComponentTypes.LEVEL)
        cells = wrapped.components.get(ItemComponentTypes.CELLS) ?: throwMissingComponentException(ItemComponentTypes.CELLS)

        // 这些理论上可以忽略不计
        rarity = wrapped.components.get(ItemComponentTypes.RARITY)?.rarity ?: RarityRegistry.DEFAULT
        elements = wrapped.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
        kizamiz = wrapped.components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz ?: emptySet()
    }

    private fun throwMissingComponentException(type: ItemComponentType<*>): Nothing {
        throw ReforgeOperationException("$PREFIX Input item has no '$type' component. This is a bug!")
    }
}