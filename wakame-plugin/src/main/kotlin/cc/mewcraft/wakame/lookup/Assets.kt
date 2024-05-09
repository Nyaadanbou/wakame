package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.asBukkit
import cc.mewcraft.wakame.util.validateAssetsPathStringOrThrow
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.Registry
import java.io.File

sealed interface Assets : Keyed {
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
    get() = requireNotNull(Registry.MATERIAL.get(ItemRegistry.INSTANCES[key].material.asBukkit)) { "Can't find material by key: $key" }