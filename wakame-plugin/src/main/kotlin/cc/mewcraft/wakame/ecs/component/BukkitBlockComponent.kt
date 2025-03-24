package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitBlock
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

// FIXME #365: 改名为 BukkitBlock
data class BukkitBlockComponent(
    val bukkitBlock: BukkitBlock,
) : Component<BukkitBlockComponent> {
    companion object : ComponentType<BukkitBlockComponent>()

    override fun type(): ComponentType<BukkitBlockComponent> = BukkitBlockComponent

    operator fun invoke(): BukkitBlock = bukkitBlock
}