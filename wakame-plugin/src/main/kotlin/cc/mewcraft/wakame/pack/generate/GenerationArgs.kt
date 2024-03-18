package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.lookup.Assets
import team.unnamed.creative.ResourcePack

data class GenerationArgs(
    val description: String,
    /**
     * The resource pack to be generated.
     *
     * Your changes will be applied to this resource pack.
     */
    val resourcePack: ResourcePack,
    val allAssets: Collection<Assets>,
)