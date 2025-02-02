package cc.mewcraft.wakame

import java.nio.file.Path

private const val CONFIGS_PATH = "configs"
private const val ASSETS_PATH = "assets"
private const val LANG_PATH = "lang"

object KoishDataPaths {
    @JvmField
    val ROOT: Path = if (SharedConstants.isRunningInIde) {
        Injector.get<Path>(InjectionQualifier.DATA_FOLDER) // 测试环境必须声明该依赖
    } else {
        Path.of("plugins/Wakame")
    }

    @JvmField
    val CONFIGS: Path = ROOT.resolve(CONFIGS_PATH)

    @JvmField
    val ASSETS: Path = ROOT.resolve(ASSETS_PATH)

    @JvmField
    val LANG: Path = ROOT.resolve(LANG_PATH)
}