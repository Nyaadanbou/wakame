@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.command.command.AttributeCommands
import cc.mewcraft.wakame.command.command.CraftCommands
import cc.mewcraft.wakame.command.command.DebugCommands
import cc.mewcraft.wakame.command.command.HephaestusCommands
import cc.mewcraft.wakame.command.command.ItemCommands
import cc.mewcraft.wakame.command.command.LootCommands
import cc.mewcraft.wakame.command.command.PluginCommands
import cc.mewcraft.wakame.command.command.ReforgeCommands
import cc.mewcraft.wakame.command.command.ResourcepackCommands
import cc.mewcraft.wakame.command.command.AbilityCommands
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.setting.ManagerSetting
import org.koin.core.component.KoinComponent

class CommandManager(
    plugin: Plugin,
) : KoinComponent, LegacyPaperCommandManager<CommandSender>(
    plugin,
    ExecutionCoordinator.asyncCoordinator(),
    SenderMapper.identity()
) {
    fun init() {
        // We are in Paper, just register Brigadier
        registerLegacyPaperBrigadier() // FIXME 等 cloud-minecraft 更新后换成 ModernPaperCommandManager
        registerAsynchronousCompletions()

        // Change default settings
        settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true)

        // Register commands
        command(AttributeCommands)
        command(CraftCommands)
        command(DebugCommands)
        command(HephaestusCommands)
        command(ItemCommands)
        command(LootCommands)
        command(PluginCommands)
        command(ReforgeCommands)
        command(ResourcepackCommands)
        command(AbilityCommands)
    }
}