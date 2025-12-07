package cc.mewcraft.wakame.messaging

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entryOrElse

/**
 * 消息队列相关配置.
 */
// 写成 object 方便其他代码直接访问
object MessagingConfig {
    private const val ROOT_PATH = "messaging"

    val enabled: Boolean by MAIN_CONFIG.entryOrElse(false, ROOT_PATH, "enabled")
    val brokerType: BrokerType by MAIN_CONFIG.entryOrElse(BrokerType.NONE, ROOT_PATH, "broker_type")

    object NATS {
        private const val NATS_PATH = "nats"

        val host: String by MAIN_CONFIG.entryOrElse("localhost", ROOT_PATH, NATS_PATH, "host")
        val port: Int by MAIN_CONFIG.entryOrElse(4222, ROOT_PATH, NATS_PATH, "port")
        val credentialsFile: String? by MAIN_CONFIG.entryOrElse(null, ROOT_PATH, NATS_PATH, "credentials_file")
    }

    object RabbitMQ {
        private const val RABBITMQ_PATH = "rabbitmq"

        val host: String by MAIN_CONFIG.entryOrElse("localhost", ROOT_PATH, RABBITMQ_PATH, "host")
        val port: Int by MAIN_CONFIG.entryOrElse(5672, ROOT_PATH, RABBITMQ_PATH, "port")
        val vhost: String by MAIN_CONFIG.entryOrElse("/", ROOT_PATH, RABBITMQ_PATH, "vhost")
        val username: String by MAIN_CONFIG.entryOrElse("user", ROOT_PATH, RABBITMQ_PATH, "user")
        val password: String? by MAIN_CONFIG.entryOrElse(null, ROOT_PATH, RABBITMQ_PATH, "pass")
    }

    object Redis {
        private const val REDIS_PATH = "redis"

        val host: String by MAIN_CONFIG.entryOrElse("localhost", ROOT_PATH, REDIS_PATH, "host")
        val port: Int by MAIN_CONFIG.entryOrElse(6379, ROOT_PATH, REDIS_PATH, "port")
        val password: String? by MAIN_CONFIG.entryOrElse(null, ROOT_PATH, REDIS_PATH, "pass")
    }

    enum class BrokerType {
        NONE,
        NATS,
        RABBITMQ,
        REDIS,
    }
}