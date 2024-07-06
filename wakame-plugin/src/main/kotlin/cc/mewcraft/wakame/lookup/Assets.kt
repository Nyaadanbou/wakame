package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.toNamespacedKey
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.Registry
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
    override val files: Collection<File> = fileStrings.map { validateAssetsPathStringOrThrow(it, "json") }
}

internal val Assets.itemType: Material
    get() = requireNotNull(
        Registry.MATERIAL.get(ItemRegistry.INSTANCES[key].itemType.toNamespacedKey)
    ) {
        "Can't find material by key: $key"
    }