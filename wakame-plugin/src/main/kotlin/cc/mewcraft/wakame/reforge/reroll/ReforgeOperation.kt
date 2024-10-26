package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.templates.components.cells.cores.EmptyCoreBlueprint
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
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

    private val logger: Logger
        get() = session.logger

    /**
     * 基于 [session] 执行一次重造流程, 并返回一个 [RerollingSession.ReforgeResult].
     */
    private fun execute(): RerollingSession.ReforgeResult {
        if (session.frozen) {
            logger.error("Trying to refresh output in a frozen session. This is a bug!")
            return ReforgeResult.error()
        }

        // 获取源物品
        // 如果源物品不存在, 返回一个空结果
        val sourceItem = session.usableInput ?: return ReforgeResult.empty()

        // 获取核孔的选择状态
        // 如果没有可重造的核孔, 返回一个失败结果
        val selectionMap = session.selectionMap
        if (!selectionMap.values.any { it.changeable }) {
            return ReforgeResult.failure("<gray>物品无法重造.".mini)
        }

        // 如果没有选择任何核孔, 返回一个失败结果
        if (!selectionMap.values.any { it.selected }) {
            return ReforgeResult.failure("<gray>没有要重造的核孔.".mini)
        }

        // 获取必要的物品组件
        val itemId = sourceItem.id
        val itemLevel = sourceItem.components.get(ItemComponentTypes.LEVEL)?.level
            ?: return ReforgeResult.failure("<gray>物品不可重造.".mini)
        val itemCells = sourceItem.components.get(ItemComponentTypes.CELLS)
            ?: return ReforgeResult.failure("<gray>物品不可重造.".mini)

        // 检查在已选择的核孔当中, 是否有超过了重造次数上限的核孔
        for (selection in selectionMap.values.filter { it.selected }) {
            val cell = itemCells.get(selection.id) ?: return ReforgeResult.error()
            val rerollCount = cell.getReforgeHistory().rerollCount
            if (rerollCount >= selection.rule.maxReroll) {
                return ReforgeResult.failure("<gray>核孔已经消磨殆尽.".mini)
            }
        }

        // 获取可有可无的物品组件
        val itemRarity = sourceItem.components.get(ItemComponentTypes.RARITY)?.rarity ?: RarityRegistry.DEFAULT
        val itemElements = sourceItem.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
        val itemKizamiz = sourceItem.components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz ?: emptySet()

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
            return ReforgeResult.error()
        }

        val updatedItemCells = itemCells.builder().apply {
            // 遍历每一个选择:
            for ((id, sel) in selectionMap) {

                // 如果玩家选择了该核孔:
                if (sel.selected) {

                    // 重新生成选择的核心 (这里跟从模板生成物品时的逻辑一样)
                    modify(id) { cell ->
                        val selected = sel.template.select(context).firstOrNull() ?: EmptyCoreBlueprint
                        val generated = selected.generate(context)
                        cell.setCore(generated)
                    }

                    // 为重造过的核孔 +1 重造次数
                    modify(id) { cell ->
                        val oldValue = cell.getReforgeHistory()
                        val newValue = oldValue.addRerollCount(1)
                        cell.setReforgeHistory(newValue)
                    }
                }
            }
        }.build()

        // 准备作为输出的物品
        val output = sourceItem.clone()

        // 将新的核孔组件写入物品
        output.components.set(ItemComponentTypes.CELLS, updatedItemCells)

        // 计算重造物品的总花费
        val total = ReforgeCost.simple(session.total.evaluate())

        return ReforgeResult.success(output, total)
    }

    /**
     * 初始化物品生成的上下文.
     */
    private fun initializeContext(
        itemId: Key,
        itemLevel: Int,
        itemRarity: Rarity,
        itemElements: Set<Element>,
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
            .filter2 { cell -> selectionMap[cell.getId()]?.selected == false }
            .forEach { (_, cell) ->
                when (
                    val core = cell.getCore()
                ) {
                    is SkillCore -> {
                        context.skills += SkillContextData(
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