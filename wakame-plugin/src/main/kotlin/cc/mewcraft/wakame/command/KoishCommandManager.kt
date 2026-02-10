@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.command.command.*
import cc.mewcraft.wakame.command.parser.AttributeParser
import cc.mewcraft.wakame.command.parser.BlockTagParser
import cc.mewcraft.wakame.command.parser.ItemParser
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.typeTokenOf
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import net.minecraft.commands.arguments.ResourceLocationArgument
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.setting.ManagerSetting

@Init(InitStage.POST_WORLD)
internal object KoishCommandManager {

    private lateinit var manager: PaperCommandManager.Bootstrapped<Source>

    fun bootstrap(context: BootstrapContext) {
        try {
            manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
                .buildBootstrapped(context)
            val brigadierManager = manager.brigadierManager()

            with(brigadierManager) {
                setNativeNumberSuggestions(true)

                // 在这里注册形如命名空间的指令参数, 否则 Brigadier 无法正常工作
                registerMapping(typeTokenOf<AttributeParser<Source>>()) { builder -> builder.cloudSuggestions().toConstant(ResourceLocationArgument.id()) }
                registerMapping(typeTokenOf<ItemParser<Source>>()) { builder -> builder.cloudSuggestions().toConstant(ResourceLocationArgument.id()) }
                registerMapping(typeTokenOf<BlockTagParser<Source>>()) { builder -> builder.cloudSuggestions().toConstant(ResourceLocationArgument.id()) }
            }

            manager.apply {
                // Change default settings
                settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true)
                settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true)

                // Register commands
                command(AttributeCommand)
                command(CatalogCommand)
                command(CraftCommand)
                command(DebugCommand)
                command(ItemCommand)
                command(NationListCommand)
                command(PluginCommand)
                command(ReforgeCommand)
                command(ResourcepackCommand)
                command(TownListCommand)
                command(TownyNetworkCommand)
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to bootstrap Koish command manager", e)
        }
    }

    @InitFun
    fun init() {
        if (!::manager.isInitialized)
            return
        manager.onEnable()
    }

}
