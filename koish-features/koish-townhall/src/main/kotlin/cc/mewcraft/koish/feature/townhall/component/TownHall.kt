package cc.mewcraft.koish.feature.townhall.component

import cc.mewcraft.koish.feature.townhall.util.TOWNY
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.palmergames.bukkit.towny.`object`.Town
import java.util.*

data class TownHall(
    val townUUID: UUID,
    val enhancements: MutableList<EnhancementType>,
    val storage: Entity,
) : Component<TownHall> {
    companion object : ComponentType<TownHall>()

    init {
        requireNotNull(TOWNY.getTown(townUUID))
    }

    val town: Town
        get() = TOWNY.getTown(townUUID)
            ?: throw IllegalStateException("Town with UUID $townUUID does not exist in Towny")

    val enhancementCount: Int
        get() = enhancements.size

    override fun type(): ComponentType<TownHall> = TownHall
}