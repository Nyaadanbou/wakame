package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.ReforgeHistory
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.getDataOrDefault
import cc.mewcraft.wakame.item2.setData
import org.bukkit.entity.Player

/**
 * 封装了一个标准的, 独立的, 定制核孔的流程.
 */
internal class ReforgeOperation
private constructor(
    private val session: SimpleModdingSession,
) {
    companion object {
        operator fun invoke(session: SimpleModdingSession): ModdingSession.ReforgeResult {
            return ReforgeOperation(session).execute()
        }
    }

    private val logger
        get() = session.logger
    private val viewer: Player
        get() = session.viewer
    private val originalInput
        get() = session.originalInput
    private val usableInput
        get() = session.usableInput
    private val replaceParams
        get() = session.replaceParams
    private val totalFunction
        get() = session.totalFunction
    private val frozen
        get() = session.frozen

    private fun execute(): ModdingSession.ReforgeResult {
        if (frozen) {
            logger.info("Trying to reforge in a frozen session. This is a bug!")
            return ReforgeResult.error(viewer)
        }

        if (originalInput == null) {
            logger.info("Skipped reforge as the source item is null")
            return ReforgeResult.empty(viewer)
        }

        // 如果源物品不存在, 则返回空
        val usableInput = usableInput ?: run {
            logger.info("Skipped reforge as source item is not legit")
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_MODDING_RESULT_FAILURE_FOR_NON_LEGIT_SOURCE_ITEM)
        }

        val itemRule = session.itemRule ?: run {
            logger.info("Skipped reforge as the item rule is null")
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_MODDING_RESULT_FAILURE_FOR_ITEM_RULE_NOT_FOUND)
        }

        // 把经过修改的核孔筛选出来
        val changedReplaceParams = replaceParams.filter { (_, repl) -> repl.originalInput != null }
        if (changedReplaceParams.isEmpty()) {
            logger.info("Skipped reforge as all replaces are not applicable")
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_MODDING_RESULT_FAILURE_FOR_ALL_CORES_NOT_APPLICABLE)
        }

        // 检查经过修改的核孔中是否存在无效的耗材
        if (changedReplaceParams.any { (_, repl) -> !repl.latestResult.applicable }) {
            logger.info("Skipped reforge as some replaces are not applicable!")
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_MODDING_RESULT_FAILURE_FOR_SOME_CORE_NOT_APPLICABLE)
        }

        // 检查经过修改的核孔中是否存在惩罚值过高
        if (changedReplaceParams.any { (_, repl) -> (repl.usableInput?.getData(ItemDataTypes.REFORGE_HISTORY)?.modCount ?: 0) >= itemRule.modLimit }) {
            logger.info("Skipped reforge as mod count exceeds mod limit")
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_MODDING_RESULT_FAILURE_FOR_REACHING_MOD_COUNT_LIMIT)
        }

        // 如果源物品不合法, 则返回失败
        val builder = usableInput.getData(ItemDataTypes.CORE_CONTAINER)?.toBuilder() ?: run {
            logger.info("No core container found in source item")
            return ReforgeResult.failure(viewer, TranslatableMessages.MSG_MODDING_RESULT_FAILURE_FOR_NO_CORE_CONTAINER_FOUND_IN_SOURCE_ITEM)
        }

        for ((id, replace) in changedReplaceParams) {
            val result = replace.latestResult
            if (replace.originalInput == null) {
                logger.info("Skipped replace '$id' as the input item is null")
                continue
            }
            if (!result.applicable) {
                logger.info("Skipped replace '$id' as the result is not applicable")
                continue
            }

            val portableCore = replace.augment!!

            builder.put(id, portableCore)
        }

        // 把修改后的核孔应用到输出物品上
        usableInput.setData(ItemDataTypes.CORE_CONTAINER, builder.build())

        // 增加物品的重铸次数
        when (session.table.reforgeCountAddMethod) {
            ModdingTable.ReforgeCountAddMethod.PLUS_ONE -> {
                val changed = usableInput.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO).incCount(1)
                usableInput.setData(ItemDataTypes.REFORGE_HISTORY, changed)
            }

            ModdingTable.ReforgeCountAddMethod.ALL_CORE -> {
                val sum = changedReplaceParams.sumOf { it.value.usableInput?.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO)?.modCount ?: 0 }
                val changed = usableInput.getDataOrDefault(ItemDataTypes.REFORGE_HISTORY, ReforgeHistory.ZERO).incCount(sum + 1)
                usableInput.setData(ItemDataTypes.REFORGE_HISTORY, changed)
            }
        }

        // 计算需要消耗的货币数量
        val reforgeCost = ReforgeCost.simple(viewer, totalFunction.evaluate())

        return ReforgeResult.success(viewer, usableInput, reforgeCost)
    }
}