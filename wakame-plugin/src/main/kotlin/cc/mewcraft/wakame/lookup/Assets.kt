package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.ItemRegistry
import net.kyori.adventure.key.Key
import org.bukkit.Material
import java.io.File

sealed interface Assets : Keyed {
    /**
     * 适用的变体.
     */
    val variant: Int

    /**
     * 所有的模型文件.
     */
    val files: Collection<File>
}

data class ItemAssets(
    override val key: Key,
    override val variant: Int,
    private val fileStrings: List<String>,
) : Assets {
    override val files: Collection<File> = fileStrings.map { AssetUtils.getFileOrThrow(it, "json") }
}

internal val Assets.itemType: Material
    get() = requireNotNull(ItemRegistry.CUSTOM.find(key)?.base?.type) {
        "ItemType not found for item id: $key"
    }