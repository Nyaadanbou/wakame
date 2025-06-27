package cc.mewcraft.wakame.ecs

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityComponentContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.UniqueId

context(context: EntityUpdateContext)
inline operator fun <reified T : Component<T>> Entity.plusAssign(component: T) {
    with(context) {
        plusAssign(component)
    }
}

context(context: EntityUpdateContext)
inline operator fun <reified T : Component<*>> Entity.minusAssign(type: ComponentType<T>) {
    with(context) {
        minusAssign(type)
    }
}

context(context: EntityComponentContext)
inline fun Entity.configure(configuration: EntityUpdateContext.(Entity) -> Unit) {
    with(context) {
        configure(configuration)
    }
}

context(context: EntityComponentContext)
inline operator fun <reified T : Component<*>> Entity.get(type: ComponentType<T>): T {
    with(context) {
        return get(type)
    }
}

context(context: EntityComponentContext)
infix fun Entity.has(type: UniqueId<*>): Boolean {
    with(context) {
        return has(type)
    }
}