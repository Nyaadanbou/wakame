package cc.mewcraft.messaging2

import cc.mewcraft.lazyconfig.access.entryOrElse
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import xyz.xenondevs.commons.provider.Provider


/* 接口 */


interface MessagingConfiguration {
    val enabled: Boolean
    val brokerType: BrokerType
    val nats: NATS
    val rabbitmq: RabbitMQ
    val redis: Redis
}

enum class BrokerType {
    NONE,
    NATS,
    RABBITMQ,
    REDIS,
}

@ConfigSerializable
data class NATS(
    val host: String = "localhost",
    val port: Int = 4222,
    val credentialsFile: String? = null,
)

@ConfigSerializable
data class RabbitMQ(
    val host: String = "localhost",
    val port: Int = 5672,
    val vhost: String = "/",
    val username: String = "user",
    val password: String? = null,
)

@ConfigSerializable
data class Redis(
    val host: String = "localhost",
    val port: Int = 6379,
    val password: String? = null,
)


/* 实现 */


/**
 * 不可变的消息队列相关配置.
 */
@ConfigSerializable
data class ImmutableMessagingConfiguration(
    override val enabled: Boolean = false,
    override val brokerType: BrokerType = BrokerType.NONE,
    override val nats: NATS = NATS(),
    override val rabbitmq: RabbitMQ = RabbitMQ(),
    override val redis: Redis = Redis(),
) : MessagingConfiguration

/**
 * 响应式的消息队列相关配置.
 */
class ReactiveMessagingConfiguration(
    provider: Provider<CommentedConfigurationNode>,
) : MessagingConfiguration {
    private val messaging by provider.entryOrElse<ImmutableMessagingConfiguration>(ImmutableMessagingConfiguration(), "messaging")

    override val enabled: Boolean
        get() = messaging.enabled
    override val brokerType: BrokerType
        get() = messaging.brokerType
    override val nats: NATS
        get() = messaging.nats
    override val rabbitmq: RabbitMQ
        get() = messaging.rabbitmq
    override val redis: Redis
        get() = messaging.redis
}
