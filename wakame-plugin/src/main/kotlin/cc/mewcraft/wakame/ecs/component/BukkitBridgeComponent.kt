package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.metadata.MetadataMap
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class BukkitBridgeComponent(
    val metadataKey: MetadataKey<ComponentBridge>,
    val metadataMapProvider: (ComponentBridge) -> MetadataMap,
) : Component<BukkitBridgeComponent> {
    companion object : ComponentType<BukkitBridgeComponent>()

    override fun type(): ComponentType<BukkitBridgeComponent> = BukkitBridgeComponent
}