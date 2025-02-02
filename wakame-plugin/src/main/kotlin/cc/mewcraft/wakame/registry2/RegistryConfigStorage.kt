package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.KoishDataPaths
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
        return KoishDataPaths.ROOT.resolve("configs").resolve(path).toFile()
    }

    fun getFileInDataDirectory(path: String): File {
        return getFileInDataDirectory(Path.of(path))
    }

    fun getFileInDataDirectory(path: Path): File {
        return KoishDataPaths.ROOT.resolve(path).toFile()
    }

}