package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.molang.Evaluable
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.Location
import org.bukkit.block.BlockFace

data class BlackHole(
    var radius: Evaluable<*>,
    var duration: Evaluable<*>,
    var damage: Evaluable<*>,
) : Component<BlackHole> {
    var holeLocation: Location? = null
    var blockFace: BlockFace = BlockFace.UP

    companion object : ComponentType<BlackHole>()

    override fun type(): ComponentType<BlackHole> = BlackHole
}
