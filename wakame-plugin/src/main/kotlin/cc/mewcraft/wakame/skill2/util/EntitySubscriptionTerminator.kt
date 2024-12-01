package cc.mewcraft.wakame.skill2.util

import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import me.lucko.helper.Events
import me.lucko.helper.event.Subscription
import org.bukkit.event.Event

class EntitySubscriptionTerminator<E : Event> private constructor(
    private val terminatorEvent: Class<E>,
    private val predicate: (E) -> Boolean,
    private val subscriptions: Set<Subscription>
) {
    companion object Factory {
        fun <E : Event> newBuilder(): Builder<E> {
            return Builder()
        }
    }

    private var isStarted = false

    fun startListen() {
        require(!isStarted) { "Already started" }

        isStarted = true
        Events.subscribe(terminatorEvent)
            .filter { predicate(it) }
            .biHandler { subscription, _ ->
                subscriptions.forEach { it.unregister() }
                subscription.unregister()
            }
    }

    class Builder<E : Event> {
        private var terminatorEvent: Class<E>? = null
        private var predicate: ((E) -> Boolean)? = null
        private val subscriptions = ReferenceArraySet<Subscription>()

        fun terminatorEvent(terminatorEvent: Class<E>): Builder<E> {
            this.terminatorEvent = terminatorEvent
            return this
        }

        fun predicate(predicate: (E) -> Boolean): Builder<E> {
            this.predicate = predicate
            return this
        }

        fun addSubscription(subscription: Subscription): Builder<E> {
            subscriptions.add(subscription)
            return this
        }

        fun addSubscriptions(subscriptions: Collection<Subscription>): Builder<E> {
            this.subscriptions.addAll(subscriptions)
            return this
        }

        fun build(): EntitySubscriptionTerminator<E> {
            requireNotNull(terminatorEvent) { "terminatorEvent is null" }
            requireNotNull(predicate) { "uniqueIdMapper is null" }

            return EntitySubscriptionTerminator(terminatorEvent!!, predicate!!, subscriptions)
        }
    }
}