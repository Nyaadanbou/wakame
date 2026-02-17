package cc.mewcraft.wakame.hook.impl.betonquest.util

import org.betonquest.betonquest.api.quest.objective.service.EventServiceSubscriptionBuilder
import org.betonquest.betonquest.api.quest.objective.service.ObjectiveService
import org.bukkit.event.Event

/* ObjectiveService */

inline fun <reified T : Event> ObjectiveService.request(): EventServiceSubscriptionBuilder<T> {
    return this.request(T::class.java)
}