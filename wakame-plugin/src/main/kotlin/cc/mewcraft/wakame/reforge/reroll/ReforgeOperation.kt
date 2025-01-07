package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.core.Holder
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.cells
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.elements
import cc.mewcraft.wakame.item.kizamiz
import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.rarity
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.item.template.AbilityContextData
import cc.mewcraft.wakame.item.template.AttributeContextData
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemGenerationContexts
import cc.mewcraft.wakame.item.template.ItemGenerationTriggers
import cc.mewcraft.wakame.item.templates.components.cells.cores.EmptyCoreArchetype
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.slf4j.Logger

/**
 * 封装了一个标准的, 独立的, 重造核孔的流程.
 */
internal class ReforgeOperation
private constructor(
    private val session: SimpleRerollingSession,
) {
    companion object : KoinComponent {
        operator fun invoke(session: SimpleRerollingSession): RerollingSession.ReforgeResult {
            return ReforgeOperation(session).execute()
        }
    }

    private val viewer: Player
        get() = session.viewer
    private val logger: Logger
        get() = session.logger

    /**
     * 基于 [session] 执行一次重造流程, 并返回一个 [RerollingSession.ReforgeResult].
     */
    private fun execute(): RerollingSession.ReforgeResult {
        if (session.frozen) {
            logger.error("Trying to refresh output in a frozen session. This is a bug!")
            return ReforgeResult.error(viewer)
        }

        // 获取 originalInput. 如果不存在, 则代表没有输入
        val originalInput = session.originalInput ?: return ReforgeResult.empty(viewer)

        // 获取 usableInput. 如果不存在, 则代表物品无法重造
        val usableInput = session.usableInput ?: return ReforgeResult.failure(viewer, MessageConstants.MSG_REROLLING_RESULT_FAILURE_INPUT_NOT_USABLE)

        // 获取 itemRule. 如果不存在, 则代表物品无法重造
        val itemRule = session.itemRule ?: return ReforgeResult.failure(viewer, MessageConstants.MSG_REROLLING_RESULT_FAILURE_ITEM_RULE_NOT_FOUND)

        // 获取核孔的选择状态. 如果没有可重造的核孔, 返回一个失败结果
        val selectionMap = session.selectionMap
        if (!selectionMap.values.any { it.changeable }) {
            return ReforgeResult.failure(viewer, MessageConstants.MSG_REROLLING_RESULT_FAILURE_NOTHING_CHANGEABLE)
        }

        // 如果没有选择任何核孔, 返回一个失败结果
        if (!selectionMap.values.any { it.selected }) {
            return ReforgeResult.failure(viewer, MessageConstants.MSG_REROLLING_RESULT_FAILURE_NOTHING_SELECTED)
        }

        // 获取必要的物品组件
        val itemId = usableInput.id
        val itemLevel = usableInput.level
        val itemCells = usableInput.cells ?: return ReforgeResult.failure(viewer, MessageConstants.MSG_REROLLING_RESULT_FAILURE_INPUT_WITHOUT_CELLS)

        // 检查重铸次数是否超过了重铸次数上限
        val modCount = usableInput.reforgeHistory.modCount
        val modLimit = itemRule.modLimit
        if (modCount >= modLimit) {
            return ReforgeResult.failure(viewer, MessageConstants.MSG_REROLLING_RESULT_FAILURE_INPUT_REACH_MOD_COUNT_LIMIT)
        }

        // 获取可有可无的物品组件
        val itemRarity = usableInput.rarity
        val itemElements = usableInput.elements
        val itemKizamiz = usableInput.kizamiz

        // 准备生成核心用的上下文
        val context = try {
            initializeContext(
                itemId,
                itemLevel,
                itemRarity,
                itemElements,
                itemKizamiz,
                itemCells
            )
        } catch (e: Exception) { // 有必要 try-catch?
            logger.error("Unexpected error while preparing generation context", e)
            return ReforgeResult.error(viewer)
        }

        val updatedItemCells = itemCells.builder().apply {
            // 遍历每一个选择:
            for ((id, sel) in selectionMap) {

                // 如果玩家选择了该核孔:
                if (sel.selected) {

                    // 重新生成选择的核心 (这里跟从模板生成物品时的逻辑一样)
                    modify(id) { cell ->
                        val selected = sel.template.select(context).firstOrNull() ?: EmptyCoreArchetype
                        val generated = selected.generate(context)
                        cell.setCore(generated)
                    }
                }
            }
        }.build()

        // 为物品的重铸历史次数 +1
        usableInput.reforgeHistory = usableInput.reforgeHistory.incCount(1)

        // 准备作为输出的物品
        val output = usableInput.clone()

        // 将新的核孔组件写入物品
        output.cells = updatedItemCells

        // 计算重造物品的总花费
        val total = ReforgeCost.simple(viewer, session.total.evaluate())

        return ReforgeResult.success(viewer, output, total)
    }

    /**
     * 初始化物品生成的上下文.
     */
    private fun initializeContext(
        itemId: Key,
        itemLevel: Int,
        itemRarity: Rarity,
        itemElements: Set<Holder<Element>>,
        itemKizamiz: Set<Kizami>,
        itemCells: ItemCells,
    ): ItemGenerationContext {
        // 创建一个空的 context
        val trigger = ItemGenerationTriggers.direct(itemLevel)
        val context = ItemGenerationContexts.create(trigger, itemId)

        // 先把*不可由玩家改变的信息*全部写入上下文
        context.level = itemLevel
        context.rarity = itemRarity
        context.elements += itemElements
        context.kizamiz += itemKizamiz

        val selectionMap = session.selectionMap

        // 然后再把*可由玩家改变的信息*全部写入上下文
        itemCells
            // 注意, 我们必须跳过玩家选择要重造的核孔.
            // 如果不跳过, 那么新的核孔将无法被正确生成.
            // 这是因为截止至 2024/8/20, 我们的设计不允许
            // 相似的核心出现在同一个物品上.
            .filter2 { cell -> !selectionMap[cell.getId()].selected }
            .forEach { (_, cell) ->
                when (
                    val core = cell.getCore()
                ) {
                    is AbilityCore -> {
                        context.abilities += AbilityContextData(
                            id = core.id
                        )
                    }

                    is AttributeCore -> {
                        context.attributes += AttributeContextData(
                            id = core.id.value(),
                            operation = core.attribute.operation,
                            element = core.attribute.element
                        )
                    }
                }
            }

        return context
    }
}