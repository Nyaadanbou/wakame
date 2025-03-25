package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.World
import org.jetbrains.annotations.ApiStatus

interface Fleks {
    companion object {
        private lateinit var INSTANCE: Fleks

        fun getInstance(): Fleks {
            if (!::INSTANCE.isInitialized) {
                throw IllegalStateException("Fleks is not initialized yet.")
            }
            return INSTANCE
        }

        @ApiStatus.Internal
        fun init(instance: Fleks) {
            INSTANCE = instance
        }

        val world: World
            get() = INSTANCE.world

        fun createEntity(configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
            return INSTANCE.createEntity(configuration)
        }

        fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
            INSTANCE.editEntity(entity, configuration)
        }
    }

    val world: World

    fun createEntity(configuration: EntityCreateContext.(Entity) -> Unit = {}): FleksEntity

    fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit)
}