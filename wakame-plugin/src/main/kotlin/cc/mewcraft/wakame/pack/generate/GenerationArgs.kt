package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.lookup.Assets
import team.unnamed.creative.ResourcePack

data class GenerationArgs(
    /**
     * The description of the resourcepack, visible to players.
     */
    val description: String,
    /**
     * The resource pack to be generated.
     *
     * Your changes will be applied to this resource pack.
     */
    val resourcePack: ResourcePack,
    /**
     * The assets contained in this resourcepack.
     */
    val allAssets: Collection<Assets>,
)