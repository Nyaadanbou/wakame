package cc.mewcraft.wakame.mixin.support

import net.kyori.adventure.key.Key
import org.bukkit.entity.Entity

/**
 * 用于在 MythicMobs 启动成功后与 Koish 交互的接口.
 */
interface MythicPluginBridge {

    fun writeIdMark(entity: Entity, id: Key)

    companion object Impl : MythicPluginBridge {

        private var implementation: MythicPluginBridge = object : MythicPluginBridge {
            override fun writeIdMark(entity: Entity, id: Key): Unit = throw IllegalStateException("MythicPluginBridge has not been initialized")
        }

        fun setImplementation(impl: MythicPluginBridge) {
            implementation = impl
        }

        override fun writeIdMark(entity: Entity, id: Key) {
            implementation.writeIdMark(entity, id)
        }
    }
}
