package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.block.Block

class BukkitBlock(
    delegate: Block,
) : Component<BukkitBlock>, ObjectWrapper<Block>(delegate) {
    companion object : ComponentType<BukkitBlock>()

    override fun type(): ComponentType<BukkitBlock> = BukkitBlock
}