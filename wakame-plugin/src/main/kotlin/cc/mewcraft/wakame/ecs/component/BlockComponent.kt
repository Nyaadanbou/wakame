package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.block.Block

data class BlockComponent(
    val block: Block,
) : Component<BlockComponent> {
    companion object : ComponentType<BlockComponent>()

    override fun type(): ComponentType<BlockComponent> = BlockComponent
}