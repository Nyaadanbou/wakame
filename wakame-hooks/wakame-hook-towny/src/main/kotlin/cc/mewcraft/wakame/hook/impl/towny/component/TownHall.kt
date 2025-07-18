package cc.mewcraft.wakame.hook.impl.towny.component

import cc.mewcraft.wakame.hook.impl.towny.TownyHook
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.palmergames.bukkit.towny.`object`.Town
import java.util.*

data class TownHall(
    val townUUID: UUID,
    val enhancements: MutableMap<TownEnhancementType, Entity>,
    val storage: Entity,
) : Component<TownHall> {
    companion object : ComponentType<TownHall>()

    init {
        requireNotNull(TownyHook.TOWNY.getTown(townUUID))
    }

    val town: Town
        get() = TownyHook.TOWNY.getTown(townUUID)
            ?: throw IllegalStateException("Town with UUID $townUUID does not exist in Towny")

    val enhancementCount: Int
        get() = enhancements.size

    override fun type(): ComponentType<TownHall> = TownHall
}