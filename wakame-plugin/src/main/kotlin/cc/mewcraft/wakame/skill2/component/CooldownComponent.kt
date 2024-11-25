package cc.mewcraft.wakame.skill2.component

import cc.mewcraft.wakame.skill2.data.Cooldown
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import me.lucko.helper.metadata.Metadata
import me.lucko.helper.metadata.MetadataKey

data class CooldownComponent(
    val cooldown: Cooldown = Cooldown(0f),
) : Component<CooldownComponent> {
    override fun type(): ComponentType<CooldownComponent> = CooldownComponent

    override fun World.onAdd(entity: Entity) {
        val bukkitEntity = entity[BukkitEntityComponent].entity
        val metadataMap = Metadata.provide(bukkitEntity)
        metadataMap.put(METADATA_KEY, this@CooldownComponent)
    }

    override fun World.onRemove(entity: Entity) {
        if (entity.isMarkedForRemoval())
            return
        val bukkitEntity = entity[BukkitEntityComponent].entity
        val metadataMap = Metadata.provide(bukkitEntity)
        metadataMap.remove(METADATA_KEY)
    }

    companion object : ComponentType<CooldownComponent>() {
        val METADATA_KEY = MetadataKey.create("cooldown", CooldownComponent::class.java)
    }
}