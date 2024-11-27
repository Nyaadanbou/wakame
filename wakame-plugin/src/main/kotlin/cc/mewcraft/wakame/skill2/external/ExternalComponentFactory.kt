package cc.mewcraft.wakame.skill2.external

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

interface ExternalComponentFactory<C : Component<*>, T : ExternalComponent<C>> {
    val externalKey: ExternalKey<T>

    fun create(component: C): T

    /**
     * 从一个 [com.github.quillraven.fleks.World] 与 [com.github.quillraven.fleks.Entity] 创建一个 [ExternalComponent]
     *
     * @return [ExternalComponent], 如果 [world] 内 [entity] 没有所需的 [Component], 则返回 null.
     */
    fun createFromEntity(world: World, entity: Entity): T?
}