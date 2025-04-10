package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import org.bukkit.block.Block

class BukkitBlock(
    delegate: Block,
) : Component<BukkitBlock>, ObjectWrapper<Block>(delegate) {
    companion object : EComponentType<BukkitBlock>()

    override fun type(): EComponentType<BukkitBlock> = BukkitBlock
}