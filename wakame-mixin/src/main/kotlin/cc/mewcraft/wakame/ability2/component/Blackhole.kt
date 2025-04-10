package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.molang.Expression
import com.github.quillraven.fleks.Component

import org.bukkit.Location
import org.bukkit.block.BlockFace

data class Blackhole(
    var radius: Expression,
    var duration: Expression,
    var damage: Expression,
) : Component<Blackhole> {
    companion object : EComponentType<Blackhole>()

    override fun type(): EComponentType<Blackhole> = Blackhole

    var holeCenter: Location? = null
    var holeDirection: BlockFace = BlockFace.UP
}
