package cc.mewcraft.wakame.util

import java.io.File

data class NamespacedFile(
    val file: File,
    val namespace: String,
    val path: String,
)

/**
 * 用于递归遍历一个文件夹中的所有文件.
 *
 * 指定文件夹下的一级子文件夹将被识别为[命名空间][NamespacedFile.namespace],
 * 每个命名空间下的所有文件(非文件夹)将被递归遍历.
 *
 * ### 参数: [includeFullPath]
 * [includeFullPath] 决定了 [NamespacedFile.path] 的生成方式:
 *
 * 1. 当 [includeFullPath] 为 `true` 时:
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
 * 在这种情况下, [NamespacedFile.path] 将包括从命名空间目录开始的相对路径, 例如 `subfolder1/file1`.
 *
 * 2. 当 [includeFullPath] 为 `false` 时:
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
 *
 * 在这种情况下, [NamespacedFile.path] 仅包含文件名, 不包含子文件夹的路径.
 *
 * @property dataDirectory 包含命名空间子目录的根目录
 * @property fileExtension 将被遍历的文件扩展名, 如果为 `null` 则遍历所有文件
 * @property includeFullPath 是否在 [NamespacedFile.path] 中包含完整的子文件夹路径
 */
class NamespacedFileTreeWalker(
    private val dataDirectory: File,
    private val fileExtension: String? = null,
    private val includeFullPath: Boolean = false,
) : Sequence<NamespacedFile> {
    override fun iterator(): Iterator<NamespacedFile> {
        // 遍历一级子目录(命名空间)
        val sequence = dataDirectory
            .walk()
            .maxDepth(1)
            .drop(1) // 排除 data directory 本身
            .filter { it.isDirectory }
            .flatMap { namespaceDirectory ->
                val namespace = namespaceDirectory.name

                // 递归遍历命名空间目录下的文件
                namespaceDirectory
                    .walk()
                    .drop(1) // 排除 namespace directory 本身
                    .filter { it.isFile && fileExtension != null && fileExtension == it.extension }
                    .map { file ->
                        val relativePath = if (includeFullPath) {
                            file.relativeTo(namespaceDirectory)
                                .invariantSeparatorsPath
                                .removeSuffix(".${file.extension}")
                        } else {
                            file.nameWithoutExtension
                        }
                        NamespacedFile(file, namespace, relativePath)
                    }

            }

        return sequence.iterator()
    }
}