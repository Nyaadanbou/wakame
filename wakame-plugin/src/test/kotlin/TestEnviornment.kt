import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.LOGGER
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

/* Common environment for running unit tests */

fun commonEnv(): Module = module {
    single<ComponentLogger> { LOGGER } withOptions { bind<Logger>() }
}

fun mainEnv(): Module = module {
    includes(commonEnv())

    val resourcesDir = Path.of("src/main/resources").absolute()
    single<Path>(InjectionQualifier.DATA_FOLDER) { resourcesDir }
    single<File>(InjectionQualifier.DATA_FOLDER) { resourcesDir.toFile() }
    single<Path>(InjectionQualifier.ASSETS_FOLDER) { resourcesDir.resolve("assets") }
    single<File>(InjectionQualifier.ASSETS_FOLDER) { resourcesDir.resolve("assets").toFile() }
    single<Path>(InjectionQualifier.CONFIGS_FOLDER) { resourcesDir.resolve("configs") }
    single<File>(InjectionQualifier.CONFIGS_FOLDER) { resourcesDir.resolve("configs").toFile() }
    single<Path>(InjectionQualifier.LANG_FOLDER) { resourcesDir.resolve("lang") }
    single<File>(InjectionQualifier.LANG_FOLDER) { resourcesDir.resolve("lang").toFile() }
}

fun testEnv(): Module = module {
    includes(commonEnv())

    val resourcesDir = Path.of("src/test/resources").absolute()
    single<Path>(InjectionQualifier.DATA_FOLDER) { resourcesDir }
    single<File>(InjectionQualifier.DATA_FOLDER) { resourcesDir.toFile() }
    single<Path>(InjectionQualifier.ASSETS_FOLDER) { resourcesDir.resolve("assets") }
    single<File>(InjectionQualifier.ASSETS_FOLDER) { resourcesDir.resolve("assets").toFile() }
    single<Path>(InjectionQualifier.CONFIGS_FOLDER) { resourcesDir.resolve("configs") }
    single<File>(InjectionQualifier.CONFIGS_FOLDER) { resourcesDir.resolve("configs").toFile() }
    single<Path>(InjectionQualifier.LANG_FOLDER) { resourcesDir.resolve("lang") }
    single<File>(InjectionQualifier.LANG_FOLDER) { resourcesDir.resolve("lang").toFile() }
}
