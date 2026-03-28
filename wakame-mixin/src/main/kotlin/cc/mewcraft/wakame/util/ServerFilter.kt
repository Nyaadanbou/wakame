package cc.mewcraft.wakame.util

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 服务器过滤器, 用于限制某些功能可在哪些服务器上使用.
 *
 * - [None]: 不限制, 所有服务器均可使用
 * - [Whitelist]: 仅允许在指定的服务器上使用
 * - [Blacklist]: 禁止在指定的服务器上使用
 */
sealed interface ServerFilter {

    /**
     * 判断给定的 serverKey 是否允许使用.
     */
    fun allows(serverKey: String): Boolean

    companion object {

        @JvmField
        val SERIALIZER: SimpleSerializer<ServerFilter> = DispatchingSerializer.createPartial(
            mapOf(
                "whitelist" to Whitelist::class,
                "blacklist" to Blacklist::class,
            )
        )
    }

    /** 不限制, 所有服务器均可使用. */
    data object None : ServerFilter {
        override fun allows(serverKey: String): Boolean = true
    }

    /** 仅允许在指定的服务器上使用. */
    @ConfigSerializable
    data class Whitelist(
        val servers: Set<String> = emptySet(),
    ) : ServerFilter {
        override fun allows(serverKey: String): Boolean = serverKey in servers
    }

    /** 禁止在指定的服务器上使用. */
    @ConfigSerializable
    data class Blacklist(
        val servers: Set<String> = emptySet(),
    ) : ServerFilter {
        override fun allows(serverKey: String): Boolean = serverKey !in servers
    }
}
