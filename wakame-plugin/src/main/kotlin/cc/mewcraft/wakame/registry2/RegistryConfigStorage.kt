package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import org.koin.core.qualifier.named
import java.io.File
import java.nio.file.Path

/**
 * 用于简化从配置文件初始化注册表的代码.
 */
interface RegistryConfigStorage {
    fun getFileInConfigDirectory(path: String): File {
        return getFileInConfigDirectory(Path.of(path))
    }

    fun getFileInConfigDirectory(path: Path): File {
        return Injector.get<File>(named(PLUGIN_DATA_DIR)).resolve(path.toString())
    }
}