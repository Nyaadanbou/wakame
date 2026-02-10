package cc.mewcraft.wakame.command.command

import cc.mewcraft.wakame.command.CommandPermissions
import cc.mewcraft.wakame.command.KoishCommandFactory
import cc.mewcraft.wakame.command.koishHandler
import cc.mewcraft.wakame.gui.towny.TownListMenu
import cc.mewcraft.wakame.util.coroutine.minecraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.kotlin.extension.getOrNull
import org.incendo.cloud.paper.util.sender.Source

internal object TownListCommand : KoishCommandFactory<Source> {

    private const val COMMAND_NAME = "townlist"

    override fun KoishCommandFactory.Builder<Source>.createCommands() {
        val builder = build(COMMAND_NAME) {
            permission(CommandPermissions.TOWN_LIST)
        }

        // <root> [player]
        buildAndAdd(builder) {
            optional("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
            koishHandler(handler = ::handlTownList)
        }
    }

    private suspend fun handlTownList(context: CommandContext<Source>) {
        val sender = context.sender().source()
        val targets = context.getOrNull<MultiplePlayerSelector>("player")?.values()
            ?: (sender as? Player)?.let(::listOf)
            ?: emptyList()

        targets.forEach { target ->
            val menu = TownListMenu(target)
            withContext(Dispatchers.minecraft) {
                menu.open()
            }
        }
    }
}