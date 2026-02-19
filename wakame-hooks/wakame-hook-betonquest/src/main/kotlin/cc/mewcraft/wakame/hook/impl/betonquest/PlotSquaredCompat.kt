package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.plot.PlotClaimActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.plot.PlotHomeActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.plot.HasPlotFactory
import cc.mewcraft.wakame.integration.Hook

@Hook(plugins = ["BetonQuest", "PlotSquared"], requireAll = true)
object PlotSquaredCompat {

    init {
        hook {
            conditions {
                register("p2has", HasPlotFactory(api.loggerFactory()))
            }
            actions {
                register("p2claim", PlotClaimActionFactory(api.loggerFactory()))
                register("p2home", PlotHomeActionFactory(api.loggerFactory()))
            }

        }
    }
}