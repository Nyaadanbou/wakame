package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.lifecycle.reloader.Reloader
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import org.bukkit.command.CommandSender
import org.incendo.cloud.context.CommandContext
import kotlin.system.measureTimeMillis

internal object PluginCommand : KoishCommandFactory<CommandSender> {

    override fun KoishCommandFactory.Builder<CommandSender>.createCommands() {
        // <root> reload config
        // Reloads the config files
        buildAndAdd {
            permission(CommandPermissions.PLUGIN)
            literal("reload")
            literal("configs")
            koishHandler(context = Dispatchers.minecraft, handler = ::handleReload)
        }
    }

    private fun handleReload(context: CommandContext<CommandSender>) {
        context.sender().sendMessage("Start reloading process, it may take a while ...")
        val reloadTime = measureTimeMillis {
            Configs.reload()
            Reloader.reload()
        }
        context.sender().sendMessage("Koish has been reloaded successfully! ${reloadTime}ms elapsed.")
    }

}