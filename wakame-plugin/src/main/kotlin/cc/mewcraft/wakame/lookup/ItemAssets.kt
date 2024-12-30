package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import java.io.File

/**
 * 代表了一个物品的所有资源文件.
 */
class ItemAssets(
    val itemId: Key,
    fileStrings: List<String>,
) {
    val files: List<File> = fileStrings.map { AssetUtils.getFileOrThrow(it, "json") }

    /**
     * 获取模型路径 key.
     */
    fun modelKey(): Key {
        return Key(RESOURCE_NAMESPACE, "item/${itemId.namespace()}/${itemId.value()}")
    }
}