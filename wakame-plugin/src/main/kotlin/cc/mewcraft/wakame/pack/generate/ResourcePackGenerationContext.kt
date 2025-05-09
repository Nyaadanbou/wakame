package cc.mewcraft.wakame.pack.generate

import team.unnamed.creative.ResourcePack

data class ResourcePackGenerationContext(
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

    //

    /**
     * 需要合并的资源包路径.
     */
    val mergePacks: List<String>,

    //

    /**
     * 本程序最终要生成的资源包 [ResourcePack].
     * 该实例的状态是*可变的*, 设计上应该被按需更新.
     * 例如, 往资源包里添加元数据, 材质, 模型, 声音.
     */
    val resourcePack: ResourcePack,
)