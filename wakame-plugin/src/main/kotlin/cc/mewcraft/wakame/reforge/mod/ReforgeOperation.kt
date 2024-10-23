package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.component.ItemComponentTypes
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
    private val sourceItem
        get() = session.sourceItem
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

        // 如果源物品不存在, 则返回空
        val output = sourceItem ?: run {
            logger.info("Skipped reforge as source item is not legit")
            return ReforgeResult.failure("<gray>物品无法定制.".mini)
        }

        // 把经过修改的核孔筛选出来
        val changedReplaceParams = replaceParams.filter { (_, repl) -> repl.hasInput }
        if (changedReplaceParams.isEmpty()) {
            logger.info("Skipped reforge as all replaces are not applicable")
            return ReforgeResult.failure("<gray>没有要修改的核心.".mini)
        }

        // 检查经过修改的核孔中是否存在无效的耗材
        if (changedReplaceParams.any { (_, repl) -> !repl.latestResult.applicable }) {
            logger.info("Skipped reforge as some replaces are not applicable")
            return ReforgeResult.failure("<gray>部分修改无法应用.".mini)
        }

        if (changedReplaceParams.any { (_, repl) -> repl.cell.getReforgeHistory().modCount >= repl.rule.modLimit }) {
            logger.info("Skipped reforge as some replaces exceed mod limit")
            return ReforgeResult.failure("<gray>部分核孔已消磨殆尽.".mini)
        }

        // 如果源物品不合法, 则返回失败
        val builder = output.components.get(ItemComponentTypes.CELLS)?.builder() ?: run {
            logger.info("No cells found in source item")
            return ReforgeResult.failure("<gray>物品不存在任何核孔.".mini)
        }

        for ((id, replace) in changedReplaceParams) {
            val result = replace.latestResult
            val ingredient = result.ingredient
            if (ingredient == null) {
                logger.info("Skipped replace '$id' as it's is not applicable")
                continue
            }
            if (!result.applicable) {
                logger.info("Skipped replace '$id' as it's is not applicable")
                continue
            }

            val portableCore = ingredient.components.get(ItemComponentTypes.PORTABLE_CORE)!!
            val wrappedCore = portableCore.wrapped

            builder.modify(id) { cell ->
                cell.setCore(wrappedCore)
                    .setReforgeHistory(
                        cell.getReforgeHistory().addModCount(1)
                    )
            }
        }

        // 把修改后的核孔应用到输出物品山
        output.components.set(ItemComponentTypes.CELLS, builder.build())

        // 计算需要消耗的货币数量
        val reforgeCost = ReforgeCost.simple(totalFunction.evaluate())

        return ReforgeResult.success(output, reforgeCost)
    }
}