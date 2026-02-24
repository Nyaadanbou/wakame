package cc.mewcraft.wakame.hook.impl.betonquest

import org.betonquest.betonquest.BetonQuest
import org.betonquest.betonquest.api.BetonQuestApi
import org.betonquest.betonquest.api.BetonQuestApiService
import org.betonquest.betonquest.api.quest.FeatureRegistry
import org.betonquest.betonquest.api.service.action.ActionRegistry
import org.betonquest.betonquest.api.service.condition.ConditionRegistry
import org.betonquest.betonquest.api.service.item.ItemRegistry
import org.betonquest.betonquest.api.service.objective.ObjectiveRegistry
import org.betonquest.betonquest.kernel.registry.feature.ScheduleRegistry
import org.betonquest.betonquest.schedule.ActionScheduling

fun hook(builder: BetonQuestHookDSL.() -> Unit) {
    BetonQuestHookDSL().apply(builder)
}

class BetonQuestHookDSL {

    val pl: BetonQuest = BetonQuest.getInstance()
    val api: BetonQuestApi = BetonQuestApiService.get().orElseThrow().api(pl)

    fun conditions(builder: ConditionRegistry.() -> Unit) {
        builder(api.conditions().registry())
    }

    fun actions(builder: ActionRegistry.() -> Unit) {
        builder(api.actions().registry())
    }

    fun objectives(builder: ObjectiveRegistry.() -> Unit) {
        builder(api.objectives().registry())
    }

    fun items(builder: ItemRegistry.() -> Unit) {
        builder(api.items().registry())
    }

    fun schedules(builder: FeatureRegistry<ActionScheduling.ScheduleType<*, *>>.() -> Unit) {
        val componentLoader = pl.componentLoader
        val scheduleRegistry = componentLoader.get(ScheduleRegistry::class.java)
        builder(scheduleRegistry)
    }
}