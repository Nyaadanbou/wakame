package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.element
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill
import cc.mewcraft.wakame.item.components.cells.template.cores.empty.TemplateCoreEmpty
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationTrigger
import cc.mewcraft.wakame.item.templates.filter.AttributeContextHolder
import cc.mewcraft.wakame.item.templates.filter.SkillContextHolder
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
internal class ReforgeOperation(
    private val session: RerollingSession,
    private val logger: Logger,
) : KoinComponent {

    /**
     * 重造台.
     */
    private val table: RerollingTable = session.table

    /**
     * 要被重造的物品; 我们将对该实例进行修改.
     */
    private val inputItem: NekoStack = session.inputItem // it's a clone

    /**
     * 玩家的选择, 包含已选择/未选择的词条栏.
     */
    private val selections: RerollingSession.SelectionMap = session.selections

    /**
     * 该对象是否已被冻结. 被冻结的对象无法再执行函数 [execute].
     */
    private var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            throw ReforgeOperationException("Unfreezing an operation is prohibited. This is a bug!")
        }
        return@vetoable true
    }

    private fun throwMissingComponent(type: ItemComponentType<*>): Nothing {
        frozen = true
        throw ReforgeOperationException("Input item has no '$type' component. This is a bug!")
    }

    /**
     * 基于要被重造的物品 ([inputItem]), 初始化上下文.
     *
     * 该上下文将被用于生成新的词条栏.
     */
    private fun generateContext(): GenerationContext {
        // 获取必要的物品信息
        val level = inputItem.components.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: throwMissingComponent(ItemComponentTypes.LEVEL)
        val cells = inputItem.components.get(ItemComponentTypes.CELLS) ?: throwMissingComponent(ItemComponentTypes.CELLS)

        // 创建一个空的 context
        val trigger = GenerationTrigger.direct(level)
        val context = GenerationContext(trigger, inputItem.key)

        // 先把*不可由玩家改变的信息*写入 context
        // 对于这些信息, 有什么就写入什么, 无需跳过
        inputItem.components.get(ItemComponentTypes.RARITY)?.rarity?.let { context.rarity = it }
        inputItem.components.get(ItemComponentTypes.ELEMENTS)?.elements?.let { context.elements += it }
        inputItem.components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz?.let { context.kizamiz += it }

        // 然后再把*可由玩家改变的信息*写入 context
        // 注意, 我们必须跳过玩家选择要重造的词条栏.
        // 如果不跳过, 那么新的词条栏将无法被正确生成.
        // 例如, 由于已存在相同的信息而最终生成了空词条栏.
        cells.filter { (id, _) -> !selections.contains(id) }
            .map { (_, cell) -> cell.getCore() }
            .forEach { core ->
                when (core) {
                    is CoreSkill ->
                        context.skills += SkillContextHolder(core.key)

                    is CoreAttribute ->
                        context.attributes += AttributeContextHolder(core.key, core.operation, core.element)
                }
            }

        return context
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
    fun execute(): SimpleRerollingSession.Result {
        if (frozen) {
            // 禁止重复执行该函数
            throw ReforgeOperationException("Trying to execute a frozen operation. This is a bug!")
        }
        // 冻结该对象
        frozen = true

        // 将要作为输出的物品.
        val outputItem = inputItem.clone()

        // 准备上下文, 用于重新生成核心.
        val context = generateContext()

        // 获取必要的物品信息.
        val level = outputItem.components.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: throwMissingComponent(ItemComponentTypes.LEVEL)
        val cells = outputItem.components.get(ItemComponentTypes.CELLS)?.builder() ?: throwMissingComponent(ItemComponentTypes.CELLS)

        // 储存词条栏的总花费 (选择/未选择).
        var sumOfEachSelected = .0
        var sumOfEachUnselected = .0

        // 遍历每一个选择:
        for ((id, sel) in selections) {

            // 计算当前词条栏的花费.
            val eachCost = table.cost.eachFunction.compute(
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
                val selected = sel.template.select(context).firstOrNull() ?: TemplateCoreEmpty
                val generated = selected.generate(context)
                cells.modify(id) { it.setCore(generated) }

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
        val compute = table.cost.totalFunction.compute(
            base = table.cost.base,
            rarity = table.cost.rarityNumberMapping.getOrDefault(context.rarityOrThrow.key, .0),
            itemLevel = level,
            allCount = selections.size,
            selectedCount = selections.count { it.selected },
            selectedCostSum = sumOfEachSelected,
            unselectedCostSum = sumOfEachUnselected,
        )
        val totalCost = SimpleRerollingSession.Result.TotalCost(compute)

        return SimpleRerollingSession.Result.success(totalCost, outputItem)
    }
}