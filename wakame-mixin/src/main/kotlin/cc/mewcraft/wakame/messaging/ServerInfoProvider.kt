package cc.mewcraft.wakame.messaging

import java.util.*

interface ServerInfoProvider {

    /**
     * 当前服务器的唯一标识符 (UUID).
     * 每次服务端启动时随机生成.
     */
    val serverId: UUID

    /**
     * 当前服务器的唯一标识符 (String).
     * 比如 "home1", "home2", "mine1", "mine2".
     */
    val serverKey: String

    /**
     * 当前服务器所属的组名.
     * 比如 "home", "mine".
     */
    val serverGroup: String

    /**
     * 当前服务器的名称.
     * 比如 "家园#1", "冒险#2".
     */
    val serverName: String

    /**
     * 持有当前 [ServerInfoProvider] 实现的伴生对象.
     */
    companion object : ServerInfoProvider {
        private val default = object : ServerInfoProvider {
            override val serverId: UUID = UUID.randomUUID()
            override val serverKey: String = "unset"
            override val serverGroup: String = "unset"
            override val serverName: String = "unset"
        }

        private var implementation: ServerInfoProvider = default

        fun setImplementation(implementation: ServerInfoProvider) {
            this.implementation = implementation
        }

        override val serverId: UUID
            get() = implementation.serverId
        override val serverKey: String
            get() = implementation.serverKey
        override val serverGroup: String
            get() = implementation.serverGroup
        override val serverName: String
            get() = implementation.serverName
    }
}