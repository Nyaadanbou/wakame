package cc.mewcraft.wakame.bridge

import net.kyori.adventure.key.Key
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntityType
import org.bukkit.entity.Entity

/**
 * 用于在 MythicMobs 启动成功后与 Koish 交互的接口.
 */
interface MythicMobsBridge {
    // Bootstrap 相关
    fun initBootstrapper()
    fun getRealEntityType(id: Identifier): EntityType<*>?

    // In-Game 相关
    fun writeIdMark(entity: Entity, id: Key)

    companion object Impl : MythicMobsBridge {
        private var implementation: MythicMobsBridge = object : MythicMobsBridge {
            override fun initBootstrapper() = throw NotImplementedError()
            override fun getRealEntityType(id: Identifier): EntityType<*> = throw NotImplementedError()
            override fun writeIdMark(entity: Entity, id: Key) = throw NotImplementedError()
        }

        fun setImplementation(impl: MythicMobsBridge) {
            implementation = impl
        }

        override fun initBootstrapper() = implementation.initBootstrapper()
        override fun getRealEntityType(id: Identifier) = implementation.getRealEntityType(id)
        override fun writeIdMark(entity: Entity, id: Key) = implementation.writeIdMark(entity, id)
    }
}
