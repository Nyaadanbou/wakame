package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.asNamespacedKey
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Material
import org.bukkit.Registry
import java.io.File

sealed interface Assets : Keyed {
    val key: Key

    override fun key(): Key = key

    val variant: Int

    val modelFiles: Collection<File>
}

data class ItemAssets(
    override val key: Key,
    override val variant: Int,
    val modelFileStrings: List<String>,
) : Assets {
    override val modelFiles: Collection<File> = modelFileStrings.map { validateAssetsPathStringOrThrow(it, "json") }
}

val Assets.material: Material
    get() = requireNotNull(Registry.MATERIAL.get(NekoItemRegistry.INSTANCES.get(key).material.asNamespacedKey)) { "Can not use $key to get material" }