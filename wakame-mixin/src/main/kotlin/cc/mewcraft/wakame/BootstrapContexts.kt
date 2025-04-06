package cc.mewcraft.wakame

import cc.mewcraft.wakame.util.data.Version
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager
import org.jetbrains.annotations.ApiStatus
import java.nio.file.Path

/**
 * 用于存放 Koish 的 [io.papermc.paper.plugin.bootstrap.BootstrapContext].
 *
 * 创建该类完全是为了能够让 mixin 模块的代码能够读取到 plugin 模块中写入的数据.
 */
object BootstrapContexts {

    @JvmField
    val IS_DEV_SERVER: Boolean = System.getProperty("KoishDev") != null

    @get:JvmName("isPluginReady")
    var PLUGIN_READY: Boolean = false
        private set

    @get:JvmName("getBootstrap")
    lateinit var BOOTSTRAP: PluginBootstrap private set

    @get:JvmName("getLifecycleManager")
    lateinit var LIFECYCLE_MANAGER: LifecycleEventManager<*> private set

    @get:JvmName("getPluginAuthors")
    lateinit var PLUGIN_AUTHORS: List<String> private set

    @get:JvmName("getPluginName")
    lateinit var PLUGIN_NAME: String private set

    @get:JvmName("getPluginVersion")
    lateinit var PLUGIN_VERSION: Version private set

    @get:JvmName("getPluginJar")
    lateinit var PLUGIN_JAR: Path private set

    @get:JvmName("getModJar")
    lateinit var MOD_JAR: Path private set

    @ApiStatus.Internal
    fun setPluginReady(ready: Boolean) {
        this.PLUGIN_READY = ready
    }

    @ApiStatus.Internal
    fun registerBootstrap(bootstrap: PluginBootstrap) {
        this.BOOTSTRAP = bootstrap
    }

    @ApiStatus.Internal
    fun registerLifecycleManager(lifecycleEventManager: LifecycleEventManager<*>) {
        this.LIFECYCLE_MANAGER = lifecycleEventManager
    }

    @ApiStatus.Internal
    fun registerAuthors(authors: List<String>) {
        this.PLUGIN_AUTHORS = authors
    }

    @ApiStatus.Internal
    fun registerName(name: String) {
        this.PLUGIN_NAME = name
    }

    @ApiStatus.Internal
    fun registerVersion(version: Version) {
        this.PLUGIN_VERSION = version
    }

    @ApiStatus.Internal
    fun registerPluginJar(jar: Path) {
        this.PLUGIN_JAR = jar
    }

    @ApiStatus.Internal
    fun registerModJar(jar: Path) {
        this.MOD_JAR = jar
    }

}