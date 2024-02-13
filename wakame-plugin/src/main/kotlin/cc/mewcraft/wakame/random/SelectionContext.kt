package cc.mewcraft.wakame.random

import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 * Represents a context that is both readable and writable by the whole
 * process of sample selection.
 *
 * @see BasicSelectionContext
 */
sealed interface SelectionContext {
    /**
     * All the [marks][Mark] that has been added to `this` context.
     */
    val marks: MutableSet<Mark<*>>
}

/**
 * You can (and should) extend this class to create your own context.
 */
open class BasicSelectionContext : SelectionContext {
    override val marks: MutableSet<Mark<*>> by SelectionContextWatcher(ObjectArraySet(8))
}

/**
 * An implementation of [ObservableProperty] for watching the changes on
 * [SelectionContext].
 */
internal class SelectionContextWatcher<V>(initialValue: V) :
    KoinComponent, ObservableProperty<V>(initialValue) {

    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    override fun afterChange(property: KProperty<*>, oldValue: V, newValue: V) {
        logger.info("[SelectionContextWatcher] ${property.name} has changed: $oldValue -> $newValue")
    }
}