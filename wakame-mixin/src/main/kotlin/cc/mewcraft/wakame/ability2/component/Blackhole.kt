package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.molang.Expression
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.Location
import org.bukkit.block.BlockFace

data class Blackhole(
    var radius: Expression,
    var duration: Expression,
    var damage: Expression,
) : Component<Blackhole> {
    companion object : ComponentType<Blackhole>()

    override fun type(): ComponentType<Blackhole> = Blackhole

    var holeCenter: Location? = null
    var holeDirection: BlockFace = BlockFace.UP
}
