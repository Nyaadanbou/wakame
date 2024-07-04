import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

/* Common environment for running unit tests */

fun testEnvironment(): Module = module {
    single<Logger> { LoggerFactory.getLogger("TestEnvironment") }
    single<File>(named(PLUGIN_DATA_DIR)) { Path.of("src/test/resources").toFile().absoluteFile }
    single<File>(named(PLUGIN_ASSETS_DIR)) { get<File>(named(PLUGIN_DATA_DIR)).resolve("assets") }
}