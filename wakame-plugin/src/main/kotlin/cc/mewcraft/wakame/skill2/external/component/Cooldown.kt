package cc.mewcraft.wakame.skill2.external.component

import cc.mewcraft.wakame.ecs.data.Cooldown as CooldownData
import cc.mewcraft.wakame.ecs.component.CooldownComponent
import cc.mewcraft.wakame.skill2.external.ExternalComponent
import cc.mewcraft.wakame.skill2.external.ExternalComponentFactory
import cc.mewcraft.wakame.skill2.external.ExternalKey
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

interface Cooldown : ExternalComponent<CooldownComponent> {

    val cooldown: CooldownData

    companion object : ExternalComponentFactory<CooldownComponent, Cooldown> {
        override val externalKey: ExternalKey<Cooldown> = ExternalKey("cooldown")

        override fun create(component: CooldownComponent): Cooldown {
            return CooldownImpl(component)
        }

        override fun createFromEntity(world: World, entity: Entity): Cooldown? {
            return with(world) { entity.getOrNull(CooldownComponent.Companion)?.let { create(it) } }
        }
    }
}

private data class CooldownImpl(
    private val component: CooldownComponent,
) : Cooldown {
    override val cooldown: CooldownData
        get() = component.cooldown

    override fun internal(): CooldownComponent {
        return component
    }
}