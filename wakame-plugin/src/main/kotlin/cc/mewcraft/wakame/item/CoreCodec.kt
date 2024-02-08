package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.ElementAttributes
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode

/**
 * Serialization operations of a [Core].
 */
interface CoreCodec<B : BinaryCoreValue, S : SchemeCoreValue> : Keyed {
    /**
     * 词条在 NBT/模板 中的唯一标识，用来定位词条的序列化实现。
     *
     * 注意，这仅仅是词条在 NBT/模板 中的唯一标识。底层由多个对象组成的词条标识就与这里的 [key] 不同。
     *
     * 例如攻击力这个属性词条，底层实际上是由两个属性组成的，分别是 [ElementAttributes.MIN_ATTACK_DAMAGE] 和
     * [ElementAttributes.MAX_ATTACK_DAMAGE]，但攻击力属性词条在 NBT/模板中的标识是一个经过整合得到的
     * `attribute:attack_damage`。
     */
    val key: Key

    override fun key(): Key = key // overrides Java interface

    /**
     * 从配置文件 [node] 创建一个模板 [S]。该函数用于从配置文件读取数据。
     */
    fun schemeOf(node: ConfigurationNode): S

    /**
     * 从模板 [scheme] 生成一个数据 [B]。该函数用于从模板生成实际的物品。
     */
    fun generate(scheme: S, scalingFactor: Int): B

    /**
     * 读取 NBT [tag] 然后将其转成一个数据 [B]。该函数用于读取 NBT 然后将其转换成一个数据容器。
     */
    fun decode(tag: CompoundShadowTag): B

    /**
     * 读取数据 [binary] 将其转成一个 NBT 标签。该函数用于将数据容器转换成对应的 NBT 标签。
     */
    fun encode(binary: B): CompoundShadowTag
}

fun interface SchemeBuilder {
    /**
     * 从配置文件的 [node] 创建一个模板值 [SchemeCoreValue]。
     */
    fun build(node: ConfigurationNode): SchemeCoreValue
}

fun interface SchemeBaker {
    /**
     * 从模板值 [scheme] 生成一个数据值 [BinaryCoreValue]。
     */
    fun bake(scheme: SchemeCoreValue, factor: Int): BinaryCoreValue
}

fun interface ShadowTagEncoder {
    /**
     * 将数据值 [value] 转换成一个 NBT。
     */
    fun encode(value: BinaryCoreValue): ShadowTag
}

fun interface ShadowTagDecoder {
    /**
     * 将 NBT [tag] 转换成一个数据值 [BinaryCoreValue]。
     */
    fun decode(tag: ShadowTag): BinaryCoreValue
}