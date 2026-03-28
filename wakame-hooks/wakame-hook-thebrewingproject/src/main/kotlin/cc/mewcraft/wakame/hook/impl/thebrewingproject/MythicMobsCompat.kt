package cc.mewcraft.wakame.hook.impl.thebrewingproject

import cc.mewcraft.wakame.hook.impl.thebrewingproject.integration.event.MythicMobsIntegration
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.HookStage
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes


@Hook(
    plugins = ["TheBrewingProject", "MythicMobs"],
    requireAll = true,
    stage = HookStage.PRE_WORLD
)
object MythicMobsCompat {

    init {
        HookUtils.withApi { api ->
            api.integrationManager.register(IntegrationTypes.EVENT, MythicMobsIntegration())
        }
    }
}