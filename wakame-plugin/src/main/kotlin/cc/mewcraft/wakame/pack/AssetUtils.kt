package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.serialization.json.GSON
import com.google.gson.JsonElement
import java.io.File

internal object AssetUtils {

    /**
     * 从给定的路径 [path] 和扩展名 [ext] 创建一个 [File].
     * 如果给定的参数没有指向一个有效的资源文件, 则返回 `null`.
     *
     * @param path 文件路径, 不带文件拓展名
     * @param ext 文件拓展名
     * @return 文件对象, 如果不存在则返回 `null`
     */
    fun getFile(path: String, ext: String): File? {
        val pathWithExt = "$path.$ext"
        val assetFile = KoishDataPaths.ASSETS.resolve(pathWithExt).toFile()
        if (!assetFile.exists()) {
            return null
        }
        if (assetFile.extension != ext) {
            return null
        }
        return assetFile
    }

    /**
     * 从给定的路径 [path] 和扩展名 [ext] 创建一个 [File].
     *
     * @param path 文件路径, 不带文件拓展名
     * @param ext 文件拓展名
     * @return 文件对象, 如果不存在则抛出异常
     */
    fun getFileOrThrow(path: String, ext: String): File {
        val file = getFile(path, ext)
        if (file == null) {
            throw IllegalArgumentException("Scroll up for the details. Invalid asset path: '$path'")
        }
        return file
    }

    /**
     * 从文件中读取 JSON.
     *
     * @param file 文件对象
     * @return [com.google.gson.JsonElement]
     */
    fun toJsonElement(file: File): JsonElement {
        val readText = file.readText()
        return GSON.fromJson(readText, JsonElement::class.java)
    }
}