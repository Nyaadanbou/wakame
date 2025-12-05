package cc.mewcraft.wakame.util.messenger

import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entryOrElse

/**
 * 消息队列相关配置.
 */
// 写成 object 方便其他代码直接访问
object MessengerConfig {
    private const val PATH_PREFIX = "messaging."

    val enabled: Boolean by MAIN_CONFIG.entryOrElse(false, PATH_PREFIX + "enabled")
    val brokerType: BrokerType by MAIN_CONFIG.entryOrElse(BrokerType.NONE, PATH_PREFIX + "broker_type")

    object NATS {
        private const val PATH_PREFIX = MessengerConfig.PATH_PREFIX + "nats."

        val host: String by MAIN_CONFIG.entryOrElse("localhost", PATH_PREFIX + "host")
        val port: Int by MAIN_CONFIG.entryOrElse(4222, PATH_PREFIX + "port")
        val credentialsFile: String? by MAIN_CONFIG.entryOrElse(null, PATH_PREFIX + "credentials_file")
    }

    object RabbitMQ {
        private const val PATH_PREFIX = MessengerConfig.PATH_PREFIX + "rabbitmq."

        val host: String by MAIN_CONFIG.entryOrElse("localhost", PATH_PREFIX + "host")
        val port: Int by MAIN_CONFIG.entryOrElse(5672, PATH_PREFIX + "port")
        val vhost: String by MAIN_CONFIG.entryOrElse("/", PATH_PREFIX + "vhost")
        val username: String by MAIN_CONFIG.entryOrElse("user", PATH_PREFIX + "user")
        val password: String by MAIN_CONFIG.entryOrElse("user", PATH_PREFIX + "pass")
    }

    object Redis {
        private const val PATH_PREFIX = MessengerConfig.PATH_PREFIX + "redis."

        val host: String by MAIN_CONFIG.entryOrElse("localhost", PATH_PREFIX + "host")
        val port: Int by MAIN_CONFIG.entryOrElse(6379, PATH_PREFIX + "port")
        val password: String? by MAIN_CONFIG.entryOrElse(null, PATH_PREFIX + "pass")
    }

    enum class BrokerType {
        NONE,
        NATS,
        RABBITMQ,
        REDIS,
    }
}