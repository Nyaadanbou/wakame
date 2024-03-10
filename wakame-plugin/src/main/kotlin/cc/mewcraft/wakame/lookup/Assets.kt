package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.registry.NekoItemRegistry
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Material
import java.io.File

sealed interface Assets : Keyed {
    val key: Key

    override fun key(): Key = key

    val sid: Int

    val modelFiles: Collection<File>
}

data class ItemAssets(
    override val key: Key,
    override val sid: Int,
    val modelFileStrings: List<String>,
) : Assets {
    override val modelFiles: Collection<File> = modelFileStrings.map { validateAssetsPathStringOrThrow(it, "json") }
}

val Assets.material: Material
    get() = NekoItemRegistry.getOrThrow(key).material