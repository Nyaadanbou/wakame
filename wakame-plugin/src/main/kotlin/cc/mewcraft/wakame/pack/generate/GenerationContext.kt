package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.lookup.Assets
import team.unnamed.creative.ResourcePack

data class GenerationContext(
    /**
     * 资源包的描述, 给玩家看的.
     */
    val description: String,
    /**
     * 最终要生成的资源包.
     *
     * 对该对象的任何修改将会应用到生成的资源包上.
     */
    val resourcePack: ResourcePack,
    /**
     * 资源包里包含的所有“资源文件”.
     */
    val assets: Collection<Assets>,
)