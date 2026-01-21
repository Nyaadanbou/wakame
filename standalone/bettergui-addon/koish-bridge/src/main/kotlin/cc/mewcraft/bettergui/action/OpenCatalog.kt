package cc.mewcraft.bettergui.action

import cc.mewcraft.wakame.catalog.CatalogType
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMainMenu
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMenuStacks
import me.hsgamer.bettergui.builder.ActionBuilder
import me.hsgamer.bettergui.util.SchedulerUtil
import me.hsgamer.hscore.action.common.Action
import me.hsgamer.hscore.common.StringReplacer
import me.hsgamer.hscore.task.element.TaskProcess
import org.bukkit.Bukkit
import java.util.*


/**
 * 打开图鉴.
 *
 * 格式:
 *
 * ```yaml
 * open-catalog: <type>
 * ```
 */
class OpenCatalog : Action {

    private val catalogType: String

    constructor(input: ActionBuilder.Input) {
        this.catalogType = input.value
    }

    override fun apply(uuid: UUID, process: TaskProcess, stringReplacer: StringReplacer) {
        val catalogType2 = CatalogType.valueOf(stringReplacer.replaceOrOriginal(catalogType, uuid).uppercase())
        when (catalogType2) {
            CatalogType.ITEM -> {
                val player = Bukkit.getPlayer(uuid) ?: run {
                    process.next()
                    return
                }
                SchedulerUtil.entity(player).run({
                    try {
                        // 如果未指定类别, 则优先打开最近一次看过的菜单
                        val last = CatalogItemMenuStacks.peek(player)
                        if (last != null) {
                            last.open()
                        } else {
                            CatalogItemMenuStacks.rewrite(player, CatalogItemMainMenu(player))
                        }
                    } finally {
                        process.next()
                    }
                }, process::next)
            }

            else -> {
                process.next()
                return
            }
        }
    }
}