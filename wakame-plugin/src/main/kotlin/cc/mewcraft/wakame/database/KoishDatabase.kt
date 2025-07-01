package cc.mewcraft.wakame.database

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

private val GLOBAL_DATABASE_CONFIG_FILE = Configs["database"]

@Init(stage = InitStage.PRE_WORLD)
object KoishDatabase {
    private val config: DatabaseConfig by GLOBAL_DATABASE_CONFIG_FILE.entry("database")

    @InitFun
    fun init() {
        connectToMariaDB()
    }

    private fun connectToMariaDB() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:mariadb://${config.host}:${config.port}/${config.database}"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = config.username
            password = config.password
            maximumPoolSize = 10
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource, setupConnection = { it.autoCommit = false })
    }
}