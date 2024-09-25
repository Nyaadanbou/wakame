@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.command.command.*
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
        command(SkillCommands)
        command(StationCommands)
    }
}