package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.World
import org.jetbrains.annotations.ApiStatus

interface Fleks {

    companion object {
        @get:JvmStatic
        @get:JvmName("getInstance")
        lateinit var INSTANCE: Fleks private set

        @ApiStatus.Internal
        fun register(instance: Fleks) {
            INSTANCE = instance
        }
    }

    val world: World

    fun createEntity(configuration: EntityCreateContext.(Entity) -> Unit = {}): FleksEntity

    fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit)
}