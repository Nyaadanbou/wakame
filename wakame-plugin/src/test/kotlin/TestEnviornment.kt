import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

/* Common environment for running unit tests */

fun commonEnv(): Module = module {
    single<Logger> { LoggerFactory.getLogger("TestEnv") }
    single<Path>(named(PLUGIN_ASSETS_DIR)) { get<Path>(named(PLUGIN_DATA_DIR)).absolute() }
    single<File>(named(PLUGIN_ASSETS_DIR)) { get<Path>(named(PLUGIN_ASSETS_DIR)).toFile() }
}

fun mainEnv(): Module = module {
    includes(commonEnv())
    single<Path>(named(PLUGIN_DATA_DIR)) { Path.of("src/main/resources").absolute() }
    single<File>(named(PLUGIN_DATA_DIR)) { get<Path>(named(PLUGIN_DATA_DIR)).toFile() }
}

fun testEnv(): Module = module {
    includes(commonEnv())
    single<Path>(named(PLUGIN_DATA_DIR)) { Path.of("src/test/resources").absolute() }
    single<File>(named(PLUGIN_DATA_DIR)) { get<Path>(named(PLUGIN_DATA_DIR)).toFile() }
}
