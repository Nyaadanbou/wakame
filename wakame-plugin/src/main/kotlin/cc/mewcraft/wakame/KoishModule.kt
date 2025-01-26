@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame

import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File
import java.nio.file.Path

private const val ASSETS_PATH = "assets"
private const val CONFIGS_PATH = "configs"
private const val LANG_PATH = "lang"

fun koishModule(): Module = module {
    single<File>(InjectionQualifier.DATA_FOLDER) { Koish.dataFolder }
    single<Path>(InjectionQualifier.DATA_FOLDER) { Koish.dataPath }
    single<File>(InjectionQualifier.ASSETS_FOLDER) { Koish.dataFolder.resolve(ASSETS_PATH) }
    single<Path>(InjectionQualifier.ASSETS_FOLDER) { Koish.dataPath.resolve(ASSETS_PATH) }
    single<File>(InjectionQualifier.CONFIGS_FOLDER) { Koish.dataFolder.resolve(CONFIGS_PATH) }
    single<Path>(InjectionQualifier.CONFIGS_FOLDER) { Koish.dataPath.resolve(CONFIGS_PATH) }
    single<File>(InjectionQualifier.LANG_FOLDER) { Koish.dataFolder.resolve(LANG_PATH) }
    single<Path>(InjectionQualifier.LANG_FOLDER) { Koish.dataPath.resolve(LANG_PATH) }
}