package cc.mewcraft.wakame.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val prefix: String,
)
