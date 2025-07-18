package cc.mewcraft.wakame.database

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MongoDataStorageConfig(
    val credentials: MongoDataStorageCredentials
)

@ConfigSerializable
data class MongoDataStorageCredentials(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val parameters: String = ""
) {
    val uri: String = "mongodb://$username:$password@$host:$port/$parameters"
}