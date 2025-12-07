package cc.mewcraft.wakame.messaging

import java.util.*

interface ServerInfoProvider {

    /**
     * 当前服务器的唯一标识符.
     */
    val serverId: UUID

    /**
     * 当前服务器的可记忆唯一标识符.
     */
    val serverMemorableId: String

    /**
     * 当前服务器所属的组名.
     */
    val serverGroupId: String

    /**
     * 当前服务器的名称.
     */
    val serverName: String

    /**
     * 持有当前 [ServerInfoProvider] 实现的伴生对象.
     */
    companion object : ServerInfoProvider {
        private val default = object : ServerInfoProvider {
            override val serverId: UUID = UUID.randomUUID()
            override val serverMemorableId: String = "unset"
            override val serverGroupId: String = "unset"
            override val serverName: String = "unset"
        }

        private var implementation: ServerInfoProvider = default

        fun setImplementation(implementation: ServerInfoProvider) {
            this.implementation = implementation
        }

        override val serverId: UUID
            get() = implementation.serverId
        override val serverMemorableId: String
            get() = implementation.serverMemorableId
        override val serverGroupId: String
            get() = implementation.serverGroupId
        override val serverName: String
            get() = implementation.serverName
    }
}