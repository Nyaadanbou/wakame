package cc.mewcraft.wakame

import cc.mewcraft.wakame.KoishDataPaths.ASSETS
import cc.mewcraft.wakame.KoishDataPaths.CONFIGS
import cc.mewcraft.wakame.KoishDataPaths.LANG
import cc.mewcraft.wakame.KoishDataPaths.ROOT
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.test.TestPath
import java.nio.file.Path
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val CONFIGS_PATH = "configs"
private const val ASSETS_PATH = "assets"
private const val LANG_PATH = "lang"

object KoishDataPaths {

    private val paths: PathSet = PathSet()

    /**
     * Koish 的根数据文件夹.
     *
     * 该文件夹的实际路径:
     * - 如果是 IDE 环境, 则为 `wakame-plugin/src/test/resources`.
     * - 如果是服务端环境, 则为 `plugins/Koish`.
     */
    @get:JvmName("getRoot")
    val ROOT: Path by paths.root

    /**
     * 配置文件夹, 位于 [ROOT] 之下的 `configs`.
     */
    @get:JvmName("getConfigs")
    val CONFIGS: Path by paths.configs

    /**
     * 资源文件夹, 位于 [ROOT] 之下的 `assets`.
     */
    @get:JvmName("getAssets")
    val ASSETS: Path by paths.assets

    /**
     * 语言文件夹, 位于 [ROOT] 之下的 `lang`.
     */
    @get:JvmName("getLang")
    val LANG: Path by paths.lang

    /**
     * 重新初始化 [ROOT], [CONFIGS], [ASSETS], [LANG] 的值.
     *
     * 对于服务端环境, 该函数会在服务端启动时调用.
     */
    fun initialize() {
        paths.root.value = Path.of("plugins/Wakame")
        paths.configs.value = ROOT.resolve(CONFIGS_PATH)
        paths.assets.value = ROOT.resolve(ASSETS_PATH)
        paths.lang.value = ROOT.resolve(LANG_PATH)
    }

    /**
     * 仅用于测试环境的配置初始化函数.
     */
    @TestOnly
    fun initializeForTest(path: TestPath) {
        paths.root.value = path.testRootPath
        paths.configs.value = ROOT.resolve(CONFIGS_PATH)
        paths.assets.value = ROOT.resolve(ASSETS_PATH)
        paths.lang.value = ROOT.resolve(LANG_PATH)
    }

    private class PathSet {

        val root = NotNullVar<Path>()
        val configs = NotNullVar<Path>()
        val assets = NotNullVar<Path>()
        val lang = NotNullVar<Path>()
    }

    private class NotNullVar<T : Any>() : ReadWriteProperty<Any?, T> {

        @JvmField
        var value: T? = null

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
        }

        override fun toString(): String =
            "NotNullProperty(${if (value != null) "value=$value" else "value not initialized yet"})"
    }

}