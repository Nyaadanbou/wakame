package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.reforgeHistory
import me.lucko.helper.text3.mini

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
            return ReforgeResult.empty()
        }

        if (originalInput == null) {
            logger.info("Skipped reforge as the source item is null")
            return ReforgeResult.empty()
        }

        // 如果源物品不存在, 则返回空
        val usableInput = usableInput ?: run {
            logger.info("Skipped reforge as source item is not legit")
            return ReforgeResult.failure("<gray>物品无法定制.".mini)
        }

        val itemRule = session.itemRule ?: run {
            logger.info("Skipped reforge as the item rule is null")
            return ReforgeResult.failure("<gray>物品无法定制.".mini)
        }

        // 把经过修改的核孔筛选出来
        val changedReplaceParams = replaceParams.filter { (_, repl) -> repl.originalInput != null }
        if (changedReplaceParams.isEmpty()) {
            logger.info("Skipped reforge as all replaces are not applicable")
            return ReforgeResult.failure("<gray>没有要修改的核心.".mini)
        }

        // 检查经过修改的核孔中是否存在无效的耗材
        if (changedReplaceParams.any { (_, repl) -> !repl.latestResult.applicable }) {
            logger.info("Skipped reforge as some replaces are not applicable")
            return ReforgeResult.failure("<gray>部分修改无法应用.".mini)
        }

        // 检查经过修改的核孔中是否存在惩罚值过高
        if (changedReplaceParams.any { (_, repl) -> (repl.usableInput?.reforgeHistory?.modCount ?: 0) >= itemRule.modLimit }) {
            logger.info("Skipped reforge as mod count exceeds mod limit")
            return ReforgeResult.failure("<gray>物品已经消磨殆尽.".mini)
        }

        // 如果源物品不合法, 则返回失败
        val builder = usableInput.components.get(ItemComponentTypes.CELLS)?.builder() ?: run {
            logger.info("No cells found in source item")
            return ReforgeResult.failure("<gray>物品不存在任何核孔.".mini)
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
            val wrappedCore = portableCore.wrapped

            builder.modify(id) { cell ->
                cell.setCore(wrappedCore)
            }
        }

        // 把修改后的核孔应用到输出物品上
        usableInput.components.set(ItemComponentTypes.CELLS, builder.build())

        // 增加重铸次数
        usableInput.reforgeHistory = usableInput.reforgeHistory.incCount(1)

        // 计算需要消耗的货币数量
        val reforgeCost = ReforgeCost.simple(totalFunction.evaluate())

        return ReforgeResult.success(usableInput, reforgeCost)
    }
}