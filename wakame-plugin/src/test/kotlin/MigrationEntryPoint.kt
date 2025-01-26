import cc.mewcraft.wakame.LegacyDataMigrator
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.Test

// 单独运行一次这个测试, 模拟迁移文件

class MigrationEntryPoint {

    @Test
    fun migrate() {
        startKoin { modules(testEnv()) }
        LegacyDataMigrator.migrate()
        stopKoin()

        startKoin { modules(mainEnv()) }
        LegacyDataMigrator.migrate()
        stopKoin()
    }

}