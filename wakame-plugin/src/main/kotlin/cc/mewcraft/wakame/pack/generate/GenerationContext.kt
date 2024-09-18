package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.lookup.Assets
import team.unnamed.creative.ResourcePack

data class GenerationContext(
    /**
     * 参考 [team.unnamed.creative.metadata.pack.PackMeta.description].
     */
    val description: String,
    /**
     * 参考 [team.unnamed.creative.metadata.pack.PackFormat.format].
     */
    val format: Int,
    /**
     * 参考 [team.unnamed.creative.metadata.pack.PackFormat.min].
     */
    val min: Int,
    /**
     * 参考 [team.unnamed.creative.metadata.pack.PackFormat.max].
     */
    val max: Int,
    /**
     * 需要合并的资源包路径.
     */
    val mergePacks: List<String>,

    //

    /**
     * 最终要生成的资源包 [ResourcePack].
     * 对该对象的任何修改将会应用到生成的资源包上.
     */
    val pack: ResourcePack,

    //

    /**
     * 资源包里包含的所有“资源文件”.
     */
    val assets: Collection<Assets>,
)