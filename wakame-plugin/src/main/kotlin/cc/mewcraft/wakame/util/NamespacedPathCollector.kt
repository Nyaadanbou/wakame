package cc.mewcraft.wakame.util

import java.io.File

data class NamespacedPath(
    val file: File,
    val namespace: String,
    val path: String,
)

/**
 * 用于收集数据目录中的命名空间路径的收集器
 *
 * @property dataDirectory 包含命名空间子目录的根目录
 * @property deepPath 是否在 `NamespacedPath` 的 `path` 属性中包含子文件夹路径
 */
class NamespacedPathCollector(
    private val dataDirectory: File,
    private val deepPath: Boolean,
) {

    /**
     * 从数据目录中收集命名空间路径.
     *
     * 该函数遍历数据目录, 识别命名空间子目录, 并收集这些子目录中的文件路径.
     * 收集的路径作为 `NamespacedPath` 对象的迭代器返回.
     *
     * `deepPath` 参数决定了路径的收集方式:
     *
     * 1. 当 `deepPath` 为 `true` 时:
     * ```
     * dataDirectory/
     * ├── namespace1/
     * │   ├── subfolder1/
     * │   │   ├── file1
     * │   │   ├── file2
     * │   │   └── ...
     * │   ├── subfolder2/
     * │   │   ├── file1
     * │   │   ├── file2
     * │   │   └── ...
     * │   └── file
     * ├── namespace2/
     * │   ├── subfolder1/
     * │   │   ├── file1
     * │   │   ├── file2
     * │   │   └── ...
     * │   └── file
     * └── ...
     * ```
     * 在这种情况下, `NamespacedPath` 的 `path` 属性将包括从命名空间目录开始的相对路径,
     * 例如 `subfolder1/file1`.
     *
     * 2. 当 `deepPath` 为 `false` 时:
     * ```
     * dataDirectory/
     * ├── namespace1/
     * │   ├── file1
     * │   ├── file2
     * │   └── ...
     * ├── namespace2/
     * │   ├── file1
     * │   ├── file2
     * │   └── ...
     * └── ...
     * ```
     * 在这种情况下, 仅收集文件的名称(不包含子文件夹路径), `NamespacedPath` 的 `path` 属性仅包含文件名.
     *
     * @param extension 文件扩展名
     * @return 代表收集路径的 `NamespacedPath` 对象的迭代器.
     */
    fun collect(extension: String): List<NamespacedPath> {
        val namespacePaths = mutableListOf<NamespacedPath>()

        // 遍历一级子目录(命名空间)
        dataDirectory.walk().maxDepth(1)
            .drop(1) // 排除 `dataDirectory` 本身
            .filter { it.isDirectory }
            .forEach { namespaceDir ->
                val namespace = namespaceDir.name
                namespaceDir.walk()
                    .drop(1) // 排除命名空间目录本身
                    .filter { it.isFile && it.extension == extension }
                    .forEach { file ->
                        val relativePath = if (deepPath) {
                            file.relativeTo(namespaceDir).path.removeSuffix(".${file.extension}").replace("\\", "/")
                        } else {
                            file.nameWithoutExtension
                        }
                        val namespacedPath = NamespacedPath(file, namespace, relativePath)
                        namespacePaths.add(namespacedPath)
                    }
            }

        return namespacePaths
    }
}