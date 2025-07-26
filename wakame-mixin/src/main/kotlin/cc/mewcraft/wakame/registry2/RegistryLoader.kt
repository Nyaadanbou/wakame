package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.FeatureDataPaths
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.feature.Feature
import java.io.File
import java.nio.file.Path

/**
 * 用于简化从配置文件初始化注册表的代码.
 */
interface RegistryLoader {

    fun getFileInConfigDirectory(path: String): File {
        return getFileInConfigDirectory(Path.of(path))
    }

    fun getFileInConfigDirectory(path: Path): File {
        return KoishDataPaths.CONFIGS.resolve(path).toFile()
    }

    fun getFileInFeatureDirectory(featureNamespace: String, path: String): File {
        return getFileInFeatureDirectory(featureNamespace, Path.of(path))
    }

    fun getFileInFeatureDirectory(featureNamespace: String, path: Path): File {
        return FeatureDataPaths.getPath(featureNamespace, path).toFile()
    }

    fun Feature.getFileInFeatureDirectory(path: String): File {
        return getFileInFeatureDirectory(namespace, Path.of(path))
    }

    fun Feature.getFileInFeatureDirectory(path: Path): File {
        return FeatureDataPaths.getPath(namespace, path).toFile()
    }

    fun getFileInDataDirectory(path: String): File {
        return getFileInDataDirectory(Path.of(path))
    }

    fun getFileInDataDirectory(path: Path): File {
        return KoishDataPaths.ROOT.resolve(path).toFile()
    }

}