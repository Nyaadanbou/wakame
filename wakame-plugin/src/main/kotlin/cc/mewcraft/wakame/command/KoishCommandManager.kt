@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.command.command.AbilityCommand
import cc.mewcraft.wakame.command.command.AttributeCommand
import cc.mewcraft.wakame.command.command.CraftCommand
import cc.mewcraft.wakame.command.command.DebugCommand
import cc.mewcraft.wakame.command.command.ItemCommand
import cc.mewcraft.wakame.command.command.PluginCommand
import cc.mewcraft.wakame.command.command.ReforgeCommand
import cc.mewcraft.wakame.command.command.ResourcepackCommand
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.setting.ManagerSetting

@Init(stage = InitStage.POST_WORLD)
internal object KoishCommandManager {

    private lateinit var manager: PaperCommandManager.Bootstrapped<Source>

    fun bootstrap(context: BootstrapContext) {
        manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildBootstrapped(context)

        manager.apply {
            // Change default settings
            settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true)
            settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true)

            // Register commands
            command(AbilityCommand)
            command(AttributeCommand)
            command(CraftCommand)
            command(DebugCommand)
            command(ItemCommand)
            command(PluginCommand)
            command(ReforgeCommand)
            command(ResourcepackCommand)
        }
    }

    @InitFun
    private fun init() {
        manager.onEnable()
    }

}
