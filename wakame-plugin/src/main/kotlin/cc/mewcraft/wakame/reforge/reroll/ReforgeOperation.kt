package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.ReforgeHistory
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.getDataOrDefault
import cc.mewcraft.wakame.item2.koishTypeId
import cc.mewcraft.wakame.item2.setData
import org.bukkit.entity.Player
import org.slf4j.Logger

/**
 * 封装了一个标准的, 独立的, 重造核孔的流程.
 */
internal class ReforgeOperation
private constructor(
    private val session: SimpleRerollingSession,
) {
    companion object {
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
        val usableInput = session.usableInput ?: return ReforgeResult.failure(viewer, TranslatableMessages.MSG_REROLLING_RESULT_FAILURE_INPUT_NOT_USABLE)

        // 获取 itemRule. 如果不存在, 则代表物品无法重造
        val itemRule = session.itemRule ?: return ReforgeResult.failure(viewer, TranslatableMessages.MSG_REROLLING_RESULT_FAILURE_ITEM_RULE_NOT_FOUND)

        // 获取核孔的选择状态. 如果没有可重造的核孔, 返回一个失败结果
        val selectionMap = session.selectionMap
        if (!selectionMap.values.any { it.changeable }) {
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_REROLLING_RESULT_FAILURE_NOTHING_CHANGEABLE)
        }

        // 如果没有选择任何核孔, 返回一个失败结果
        if (!selectionMap.values.any { it.selected }) {
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_REROLLING_RESULT_FAILURE_NOTHING_SELECTED)
        }

        // 获取必要的物品组件
        val itemId = usableInput.koishTypeId!!
        val itemLevel = usableInput.getData(ItemDataTypes.LEVEL)?.level!!
        val itemCoreContainer = usableInput.getData(ItemDataTypes.CORE_CONTAINER) ?: return ReforgeResult.failure(viewer, TranslatableMessages.MSG_REROLLING_RESULT_FAILURE_INPUT_WITHOUT_CELLS)

        // 检查重铸次数是否超过了重铸次数上限
        val modCount = usableInput.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO).modCount
        val modLimit = itemRule.modLimit
        if (modCount >= modLimit) {
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_REROLLING_RESULT_FAILURE_INPUT_REACH_MOD_COUNT_LIMIT)
        }

        // 获取可有可无的物品组件
        val itemRarity = usableInput.getData(ItemDataTypes.RARITY)!!
        val itemElements = usableInput.getData(ItemDataTypes.ELEMENT)!!
        val itemKizamiz = usableInput.getData(ItemDataTypes.KIZAMI)!!

        // 准备生成核心用的上下文
        // val context = try {
        //    initializeContext(
        //        itemId,
        //        itemLevel,
        //        itemRarity,
        //        itemElements,
        //        itemKizamiz,
        //        itemCoreContainer
        //    )
        // } catch (e: Exception) { // 有必要 try-catch?
        //    logger.error("Unexpected error while preparing generation context", e)
        //    return ReforgeResult.error(viewer)
        // }

        // val updatedCoreContainer = itemCoreContainer.toBuilder().apply {
        //    // 遍历每一个选择:
        //    for ((id, sel) in selectionMap) {
        //
        //        // 如果玩家选择了该核孔:
        //        if (sel.selected) {
        //            // 重新生成选择的核心 (这里跟从模板生成物品时的逻辑一样)
        //
        //            val selected = sel.template.select(context).firstOrNull() ?: EmptyCoreArchetype
        //            val generated = selected.generate(context)
        //            put(id, generated)
        //        }
        //    }
        // }.build()
        val updatedCoreContainer = itemCoreContainer // TODO: 生成新的核孔

        // 为物品的重铸历史次数 +1
        usableInput.setData(ItemDataTypes.REFORGE_HISTORY, usableInput.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO).incCount(1))

        // 准备作为输出的物品
        val output = usableInput.clone()

        // 将新的核孔组件写入物品
        output.setData(ItemDataTypes.CORE_CONTAINER, updatedCoreContainer)

        // 计算重造物品的总花费
        val total = ReforgeCost.simple(viewer, session.total.evaluate())

        return ReforgeResult.success(viewer, output, total)
    }

//    /**
//     * 初始化物品生成的上下文.
//     */
//    private fun initializeContext(
//        itemId: Key,
//        itemLevel: Int,
//        itemRarity: RegistryEntry<Rarity>,
//        itemElements: Set<RegistryEntry<Element>>,
//        itemKizamiz: Set<RegistryEntry<Kizami>>,
//        itemCoreContainer: CoreContainer,
//    ): Context {
//        // 创建一个空的 context
//        val context = Context(BuiltInRegistries.ITEM.getDefaultEntry())
//
//        // 先把*不可由玩家改变的信息*全部写入上下文
//        context.level = itemLevel
//        context.rarity = itemRarity
//        context.elements += itemElements
//        context.kizamiz += itemKizamiz
//
//        val selectionMap = session.selectionMap
//
//        // 然后再把*可由玩家改变的信息*全部写入上下文
//        itemCoreContainer
//            // 注意, 我们必须跳过玩家选择要重造的核孔.
//            // 如果不跳过, 那么新的核孔将无法被正确生成.
//            // 这是因为截止至 2024/8/20, 我们的设计不允许
//            // 相似的核心出现在同一个物品上.
//            .filter { id, core -> !selectionMap[id].selected }
//            .forEach { (id, core) ->
//                when (core) {
//                    is AttributeCore -> {
//                        context.attributes += AttributeContextData(
//                            id = id,
//                            operation = core.wrapped.operation,
//                            element = core.wrapped.element
//                        )
//                    }
//                    EmptyCore -> {
//                        // NOP
//                    }
//                    VirtualCore -> {
//                        // NOP
//                    }
//                }
//            }
//
//        return context
//    }
}