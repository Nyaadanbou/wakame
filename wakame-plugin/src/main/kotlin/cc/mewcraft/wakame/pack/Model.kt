package cc.mewcraft.wakame.pack

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Material
import java.io.File

interface Model : Keyed {

    val key: Key

    // Overrides Java's getter
    override fun key(): Key = key

    val originalModelMaterial: Material

    /**
     * Gets the model path of this [Model].
     *
     * The model path rules are:
     *  - The model file should exist.
     *  - The model path should be a relative path to the assets directory.
     *  (e.g. "models/item/short_sword/neko_item.json")
     *  - The model path should end with ".json".
     */
    val modelFile: File?
}