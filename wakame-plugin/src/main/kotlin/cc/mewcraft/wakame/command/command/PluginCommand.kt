package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.init.ItemInitializer
import cc.mewcraft.wakame.init.RecipeInitializer
import cc.mewcraft.wakame.item.ItemTagManager
import cc.mewcraft.wakame.lifecycle.reloader.Reloader
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.EnumParser
import kotlin.system.measureTimeMillis


// TODO 在 #439 后续的 PR 中慢慢完善此指令

internal object PluginCommand : KoishCommandFactory<Source> {

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        // <root> reload <type>
        // Reload specific plugin configs
        buildAndAdd {
            permission(CommandPermissions.PLUGIN)
            literal("reload")
            required("type", EnumParser.enumParser(ReloadType::class.java))
            koishHandler(context = Dispatchers.minecraft, handler = ::handleReload)
        }
    }

    private fun handleReload(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val reloadType = context.get<ReloadType>("type")

        sender.sendMessage("Starting reload process, it may take a while...")

        val elapsed = measureTimeMillis {
            when (reloadType) {
                ReloadType.ALL -> {
                    Reload.all()
                }

                ReloadType.ITEM -> {
                    Reload.items()
                }

                ReloadType.RECIPE -> {
                    Reload.recipes()
                }

                ReloadType.TAG -> {
                    Reload.tags()
                }
            }
        }

        sender.sendMessage("Reload OK, ${elapsed}ms elapsed.")
    }

    enum class ReloadType {
        ALL, // 所有配置

        /* 按字母排序 */

        ITEM, // 物品
        RECIPE, // 配方
        TAG, // 标签
    }
}

/**
 * 封装了各模块的重载逻辑.
 *
 * 注意这里不应该隐式的包含任何依赖关系.
 * 每个重载逻辑都只负责自己的内容 (即使这会导致最终的数据关系错乱).
 * 依赖关系的维护应该交给调用方 (也就是使用重载指令的人).
 */
private object Reload {

    fun all() {
        // TODO 目前用的是注解方式来调用整个重载逻辑, 之后直接手写整个重载逻辑吧
        Configs.reload()
        Reloader.reload()
    }

    fun tags() {
        ItemTagManager.reload()
    }

    fun items() {
        ItemInitializer.reload()
    }

    fun recipes() {
        RecipeInitializer.reload()
    }
}