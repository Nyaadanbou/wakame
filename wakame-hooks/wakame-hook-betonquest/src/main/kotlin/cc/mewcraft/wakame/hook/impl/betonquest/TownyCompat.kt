package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny.JoinsMarketNetworkFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny.LeavesMarketNetworkFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny.OperateGovernmentBankFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny.UpdateGovernmentBoardFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.GovernmentBankBalanceFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.JoinedMarketNetworkFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.TownyRankFactory
import cc.mewcraft.wakame.integration.Hook

@Hook(plugins = ["BetonQuest", "Towny"], requireAll = true)
object TownyCompat {

    init {
        hook {
            conditions {
                register("townyRank", TownyRankFactory(api.loggerFactory()))
                register("bankBalance", GovernmentBankBalanceFactory(api.loggerFactory()))
                register("joinedMarketNetwork", JoinedMarketNetworkFactory(api.loggerFactory()))
            }
            actions {
                register("joinsMarketNetwork", JoinsMarketNetworkFactory(api.loggerFactory()))
                register("leavesMarketNetwork", LeavesMarketNetworkFactory(api.loggerFactory()))
                register("operateGovernmentBank", OperateGovernmentBankFactory(api.loggerFactory()))
                register("updateGovernmentBoard", UpdateGovernmentBoardFactory(api.loggerFactory()))
            }
        }
    }
}