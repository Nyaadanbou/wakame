package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.towny.*
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.GovernmentBankBalanceFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.HasJoinedMarketNetworkFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.HasPaidMarketNetworkTaxFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.towny.TownyRankFactory
import cc.mewcraft.wakame.integration.Hook
import org.betonquest.betonquest.BetonQuest

@Hook(plugins = ["BetonQuest", "Towny"], requireAll = true)
object TownyCompat {

    init {
        val plugin = BetonQuest.getInstance()
        val questTypeApi = plugin.questTypeApi
        val profileProvider = plugin.profileProvider
        val loggerFactory = plugin.loggerFactory

        /* Quest Type Registries */

        // Condition
        val conditionRegistry = plugin.questRegistries.condition()
        conditionRegistry.register("townyRank", TownyRankFactory(loggerFactory))
        conditionRegistry.register("bankBalance", GovernmentBankBalanceFactory(loggerFactory))
        conditionRegistry.register("hasJoinedMarketNetwork", HasJoinedMarketNetworkFactory(loggerFactory))
        conditionRegistry.register("hasPaidMarketNetworkTax", HasPaidMarketNetworkTaxFactory(loggerFactory))

        // Action
        val actionRegistry = plugin.questRegistries.action()
        actionRegistry.register("joinsMarketNetwork", JoinsMarketNetworkFactory(loggerFactory))
        actionRegistry.register("leavesMarketNetwork", LeavesMarketNetworkFactory(loggerFactory))
        actionRegistry.register("paysMarketNetworkTax", PaysMarketNetworkTaxFactory(loggerFactory))
        actionRegistry.register("operateGovernmentBank", OperateGovernmentBankFactory(loggerFactory))
        actionRegistry.register("updateGovernmentBoard", UpdateGovernmentBoardFactory(loggerFactory))

        // Objective
        val objectiveRegistry = plugin.questRegistries.objective()

        // Variable
        val placeholderRegistry = plugin.questRegistries.placeholder()

        /* Feature Registries */

        // ConversationIO
        val conversationIORegistry = plugin.featureRegistries.conversationIO()

        // Interceptor
        val interceptorRegistry = plugin.featureRegistries.interceptor()

        // Item
        val itemRegistry = plugin.featureRegistries.item()

        // TextParser
        val textParserRegistry = plugin.featureRegistries.textParser()

        // NotifyIO
        val notifyIORegistry = plugin.featureRegistries.notifyIO()

        // Schedule
        val scheduleRegistry = plugin.featureRegistries.actionScheduling()
    }
}