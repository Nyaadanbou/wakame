package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import java.io.File

internal object AssetUtils {
    private val LOGGER: Logger = Injector.get()
    private val ASSET_DIR: File by Injector.inject(named(PLUGIN_ASSETS_DIR))

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
        val assetFile = ASSET_DIR.resolve(pathWithExt)
        if (!assetFile.exists()) {
            LOGGER.warn("No such file: '$assetFile'")
            return null
        }
        if (assetFile.extension != ext) {
            LOGGER.warn("Invalid file extension: '$assetFile', expected: '$ext'")
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
     * @return [JsonElement]
     */
    fun toJsonElement(file: File): JsonElement {
        val readText = file.readText()
        val gson = Injector.get<Gson>()
        return gson.fromJson(readText, JsonElement::class.java)
    }
}