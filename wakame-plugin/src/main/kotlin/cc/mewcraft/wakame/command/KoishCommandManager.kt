@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.command.command.*
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.setting.ManagerSetting

@Init(
    stage = InitStage.PRE_WORLD,
)
internal object KoishCommandManager {

    @InitFun
    fun init() {
        val manager = LegacyPaperCommandManager(
            Koish,
            ExecutionCoordinator.asyncCoordinator(),
            SenderMapper.identity()
        )

        manager.apply {
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

}
