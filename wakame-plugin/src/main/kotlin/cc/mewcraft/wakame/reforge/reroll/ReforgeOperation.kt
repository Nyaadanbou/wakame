package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.NekoStack
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
import me.lucko.helper.text3.mini
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * 封装了一个标准的, 独立的, 重造流程.
 */
internal class ReforgeOperation(
    private val session: RerollingSession,
) : KoinComponent {

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.REROLL
    }

    private val logger: Logger by inject()

    /**
     * 要被重造的物品; 我们将原地对该实例进行修改.
     */
    private val sourceItem: NekoStack? = session.sourceItem

    /**
     * 基于 [session] 和 [sourceItem] 执行一次重造流程, 并返回一个 [RerollingSession.Result].
     */
    fun execute(): RerollingSession.Result {
        if (session.frozen) {
            logger.error("$PREFIX Trying to refresh output in a frozen session. This is a bug!")
            return Result.error()
        }

        // 获取源物品
        // 如果源物品不存在, 返回一个空结果
        val sourceItem = sourceItem ?: return Result.empty()

        // 获取词条栏的选择状态
        // 如果没有可重造的词条栏, 返回一个失败结果
        val selectionMap = session.selectionMap
        if (selectionMap.isEmpty || selectionMap.values.all { !it.changeable }) {
            return Result.failure("<gray>没有可重造的词条栏".mini)
        }

        // 如果没有选择任何词条栏, 返回一个失败结果
        if (selectionMap.values.all { !it.selected }) {
            return Result.failure("<gray>没有选择任何词条栏".mini)
        }

        // region 准备作为输出的物品
        val output = sourceItem.clone()

        // 获取必要的物品组件
        val itemId = sourceItem.id
        val itemLevel = sourceItem.components.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: return Result.failure("<gray>物品不可重造".mini)
        val itemCells = sourceItem.components.get(ItemComponentTypes.CELLS) ?: return Result.failure("<gray>物品不可重造".mini)

        // 获取可有可无的物品组件
        val itemRarity = sourceItem.components.get(ItemComponentTypes.RARITY)?.rarity ?: RarityRegistry.DEFAULT
        val itemElements = sourceItem.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
        val itemKizamiz = sourceItem.components.get(ItemComponentTypes.KIZAMIZ)?.kizamiz ?: emptySet()

        // 准备生成核心用的上下文
        val context = try {
            generateContext(
                itemId,
                itemLevel,
                itemRarity,
                itemElements,
                itemKizamiz,
                itemCells
            )
        } catch (e: Exception) { // 有必要 try-catch?
            logger.error("$PREFIX Unexpected error while preparing generation context", e)
            return Result.error()
        }

        val cellsBuilder = itemCells.builder()

        // 遍历每一个选择:
        for ((id, sel) in selectionMap) {

            // 如果玩家选择了该词条栏:
            if (sel.selected) {

                // 重新生成选择的核心 (这里跟从模板生成物品时的逻辑一样)
                cellsBuilder.modify(id) { cell ->
                    val selected = sel.template.select(context).firstOrNull() ?: TemplateCoreEmpty
                    val generated = selected.generate(context)
                    cell.setCore(generated)
                }

                // 为重造过的词条栏 +1 重造次数
                cellsBuilder.modify(id) { cell ->
                    val oldValue = cell.getReforgeHistory()
                    val newValue = oldValue.addRerollCount(1)
                    cell.setReforgeHistory(newValue)
                }
            }
        }

        // 将新的词条栏组件写入物品
        output.components.set(ItemComponentTypes.CELLS, cellsBuilder.build())
        // endregion

        // region 计算重造物品的总花费
        val total = Cost.simple(session.total.evaluate())
        // endregion

        return Result.success(output, total)
    }

    /**
     * 初始化物品生成的上下文.
     */
    private fun generateContext(
        itemId: Key,
        itemLevel: Int,
        itemRarity: Rarity,
        itemElements: Set<Element>,
        itemKizamiz: Set<Kizami>,
        itemCells: ItemCells,
    ): GenerationContext {
        // 创建一个空的 context
        val trigger = GenerationTrigger.direct(itemLevel)
        val context = GenerationContext(trigger, itemId)

        // 先把*不可由玩家改变的信息*全部写入上下文
        context.level = itemLevel.toShort()
        context.rarity = itemRarity
        context.elements += itemElements
        context.kizamiz += itemKizamiz

        val selectionMap = session.selectionMap

        // 然后再把*可由玩家改变的信息*全部写入上下文
        itemCells
            .filterx { cell ->
                // 注意, 我们必须跳过玩家选择要重造的词条栏.
                // 如果不跳过, 那么新的词条栏将无法被正确生成.
                // 这是因为截止至 2024/8/20, 我们的设计不允许
                // 相似的核心出现在同一个物品上.
                selectionMap[cell.getId()]?.selected == false
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

        return context
    }
}