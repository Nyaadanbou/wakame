package cc.mewcraft.wakame.hook.impl.betonquest

import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish.LockFreezeTicksActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish.ReplaceCrateKeyActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish.SetFreezeTicksActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.koish.TeleportOnJoinActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.party.CreatePartyActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.action.party.LeavePartyActionFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish.AttributeFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish.LightFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.koish.OutsideFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.condition.party.HasPartyFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.item.KoishQuestItemSerializer
import cc.mewcraft.wakame.hook.impl.betonquest.quest.objective.ChangeWorldObjectiveFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule.GameTickScheduleFactory
import cc.mewcraft.wakame.hook.impl.betonquest.quest.schedule.GameTickScheduler
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.betonquest.BetonQuestIntegration
import org.betonquest.betonquest.schedule.ActionScheduling

@Hook(plugins = ["BetonQuest"])
object BetonQuestHook {

    init {
        BetonQuestIntegration.setImplementation(BetonQuestIntegrationImpl())

        hook {
            conditions {
                register("attribute", AttributeFactory(api.loggerFactory()))
                register("hasparty", HasPartyFactory(api.loggerFactory()))
                register("light", LightFactory(api.loggerFactory()))
                register("outside", OutsideFactory(api.loggerFactory()))
            }
            actions {
                register("createparty", CreatePartyActionFactory(api.loggerFactory(), api.conditions().manager(), api.profiles()))
                register("leaveparty", LeavePartyActionFactory(api.loggerFactory()))
                register("lockfreezeticks", LockFreezeTicksActionFactory(api.loggerFactory()))
                register("setfreezeticks", SetFreezeTicksActionFactory(api.loggerFactory()))
                register("teleportonjoin", TeleportOnJoinActionFactory(api.loggerFactory()))
                register("replaceCrateKey", ReplaceCrateKeyActionFactory(api.loggerFactory()))
            }
            objectives {
                register("changeworld", ChangeWorldObjectiveFactory())
            }
            items {
                register("koish", KoishQuestItemFactory())
                registerSerializer("koish", KoishQuestItemSerializer())
            }
            schedules {
                register(
                    "game-tick",
                    ActionScheduling.ScheduleType(
                        GameTickScheduleFactory(),
                        GameTickScheduler(api.loggerFactory().create(GameTickScheduler::class.java), api.actions().manager(), pl)
                    ),
                )
            }
        }
    }
}
