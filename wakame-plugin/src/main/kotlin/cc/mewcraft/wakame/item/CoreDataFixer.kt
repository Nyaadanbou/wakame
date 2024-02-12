package cc.mewcraft.wakame.item

import me.lucko.helper.shadows.nbt.ShadowTag
import org.spongepowered.configurate.ConfigurationNode

/**
 * 从配置文件 [ConfigurationNode] 创建一个模板值 [SchemeCoreValue].
 */
fun interface SchemeBuilder {
    fun build(node: ConfigurationNode): SchemeCoreValue
}

/**
 * 从模板值 [SchemeCoreValue] (随机)生成一个数据值 [BinaryCoreValue].
 */
fun interface SchemeBaker {
    fun bake(scheme: SchemeCoreValue, factor: Int): BinaryCoreValue
}

/**
 * 将数据值 [BinaryCoreValue] 转换成一个 NBT [ShadowTag].
 */
fun interface ShadowTagEncoder {
    fun encode(value: BinaryCoreValue): ShadowTag
}

/**
 * 将 NBT [ShadowTag] 转换成一个数据值 [BinaryCoreValue].
 */
fun interface ShadowTagDecoder {
    fun decode(tag: ShadowTag): BinaryCoreValue
}