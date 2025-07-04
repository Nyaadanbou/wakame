package cc.mewcraft.wakame.hook.impl.towny.component

import cc.mewcraft.wakame.hook.impl.towny.TownyHook
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.palmergames.bukkit.towny.`object`.Town
import java.util.*

data class TownHall(
    val townUUID: UUID,
) : Component<TownHall> {
    companion object : ComponentType<TownHall>()

    val town: Town
        get() = TownyHook.TOWNY.getTown(townUUID)
            ?: throw IllegalStateException("Town with UUID $townUUID does not exist in Towny")

    override fun type(): ComponentType<TownHall> = TownHall
}