import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.LOGGER
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import org.slf4j.Logger
import java.nio.file.Path

/* Common environment for running unit tests */

fun commonEnv(): Module = module {
    single<ComponentLogger> { LOGGER } withOptions { bind<Logger>() }
}

fun mainEnv(): Module = module {
    includes(commonEnv())

    val resourcesDir = Path.of("src/main/resources")
    single<Path>(InjectionQualifier.DATA_FOLDER) { resourcesDir }
    single<Path>(InjectionQualifier.CONFIGS_FOLDER) { resourcesDir.resolve("configs") }
    single<Path>(InjectionQualifier.ASSETS_FOLDER) { resourcesDir.resolve("assets") }
    single<Path>(InjectionQualifier.LANG_FOLDER) { resourcesDir.resolve("lang") }
}

fun testEnv(): Module = module {
    includes(commonEnv())

    val resourcesDir = Path.of("src/test/resources")
    single<Path>(InjectionQualifier.DATA_FOLDER) { resourcesDir }
    single<Path>(InjectionQualifier.CONFIGS_FOLDER) { resourcesDir.resolve("configs") }
    single<Path>(InjectionQualifier.ASSETS_FOLDER) { resourcesDir.resolve("assets") }
    single<Path>(InjectionQualifier.LANG_FOLDER) { resourcesDir.resolve("lang") }
}
