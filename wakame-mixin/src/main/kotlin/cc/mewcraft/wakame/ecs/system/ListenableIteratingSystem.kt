package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import com.github.quillraven.fleks.Automatic
import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.Interval
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.SortingType
import com.github.quillraven.fleks.collection.EntityComparator
import org.bukkit.event.Listener

abstract class ListenableIteratingSystem(
    family: Family,
    comparator: EntityComparator = EMPTY_COMPARATOR,
    sortingType: SortingType = Automatic,
    interval: Interval = EachFrame,
    enabled: Boolean = true,
) : Listener, IteratingSystem(family, comparator, sortingType, interval, enabled) {

    override fun onInit() {
        registerEvents()
    }

    override fun onDisable() {
        unregisterEvents()
    }

    companion object {
        private val EMPTY_COMPARATOR = EntityComparator { _, _ -> 0 }
    }
}