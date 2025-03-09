package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.molang.Evaluable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.Location
import org.bukkit.block.BlockFace

data class Blackhole(
    var radius: Evaluable<*>,
    var duration: Evaluable<*>,
    var damage: Evaluable<*>,
) : Component<Blackhole> {
    companion object : ComponentType<Blackhole>()

    override fun type(): ComponentType<Blackhole> = Blackhole

    var holeCenter: Location? = null
    var holeDirection: BlockFace = BlockFace.UP
}
