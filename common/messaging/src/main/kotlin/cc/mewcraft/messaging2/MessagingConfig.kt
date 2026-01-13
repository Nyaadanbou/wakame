package cc.mewcraft.messaging2

import cc.mewcraft.lazyconfig.access.entryOrElse
import org.spongepowered.configurate.CommentedConfigurationNode
import xyz.xenondevs.commons.provider.Provider

/**
 * 消息队列相关配置.
 */
class MessagingConfig(
    private val provider: Provider<CommentedConfigurationNode>,
) {
    companion object {
        private const val ROOT_PATH = "messaging"
        private const val NATS_PATH = "nats"
        private const val RABBITMQ_PATH = "rabbitmq"
        private const val REDIS_PATH = "redis"
    }

    val enabled: Boolean by provider.entryOrElse(false, ROOT_PATH, "enabled")
    val brokerType: BrokerType by provider.entryOrElse(BrokerType.NONE, ROOT_PATH, "broker_type")

    inner class NATS {

        val host: String by provider.entryOrElse("localhost", ROOT_PATH, NATS_PATH, "host")
        val port: Int by provider.entryOrElse(4222, ROOT_PATH, NATS_PATH, "port")
        val credentialsFile: String? by provider.entryOrElse(null, ROOT_PATH, NATS_PATH, "credentials_file")
    }

    inner class RabbitMQ {

        val host: String by provider.entryOrElse("localhost", ROOT_PATH, RABBITMQ_PATH, "host")
        val port: Int by provider.entryOrElse(5672, ROOT_PATH, RABBITMQ_PATH, "port")
        val vhost: String by provider.entryOrElse("/", ROOT_PATH, RABBITMQ_PATH, "vhost")
        val username: String by provider.entryOrElse("user", ROOT_PATH, RABBITMQ_PATH, "user")
        val password: String? by provider.entryOrElse(null, ROOT_PATH, RABBITMQ_PATH, "pass")
    }

    inner class Redis {

        val host: String by provider.entryOrElse("localhost", ROOT_PATH, REDIS_PATH, "host")
        val port: Int by provider.entryOrElse(6379, ROOT_PATH, REDIS_PATH, "port")
        val password: String? by provider.entryOrElse(null, ROOT_PATH, REDIS_PATH, "pass")
    }

    enum class BrokerType {

        NONE,
        NATS,
        RABBITMQ,
        REDIS,
    }
}

