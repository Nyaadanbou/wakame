package cc.mewcraft.extracontexts.common.database

/**
 * Supported database types.
 */
enum class DatabaseType {
    SQLITE,
    H2,
    MYSQL,
    MARIADB,
    POSTGRESQL;

    companion object {
        fun fromString(value: String): DatabaseType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unknown database type: $value. Supported types: ${values().joinToString(", ") { it.name }}")
            }
        }
    }
}

