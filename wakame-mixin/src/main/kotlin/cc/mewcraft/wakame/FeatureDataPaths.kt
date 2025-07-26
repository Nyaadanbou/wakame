package cc.mewcraft.wakame

import cc.mewcraft.wakame.feature.Feature
import java.nio.file.Path

object FeatureDataPaths {

    /**
     * 获取指定命名空间的根数据路径.
     */
    fun getRootPath(namespace: String): Path {
        return KoishDataPaths.FEATURES.resolve(namespace)
    }

    /**
     * 获取指定命名空间下的文件路径.
     */
    fun getPath(namespace: String, filePath: Path): Path {
        return getRootPath(namespace).resolve(filePath)
    }

    /**
     * 获取指定 Feature 的根数据路径.
     */
    fun Feature.getPath(filePath: Path): Path {
        return getPath(namespace, filePath)
    }

}