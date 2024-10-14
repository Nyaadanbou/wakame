package cc.mewcraft.wakame.item.template

import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.util.stream.Stream

// 开发日记 2024/7/5
// 这里的类型 T 不能设置 upper bounds,
// 因为存在数据类型为 primitive 的组件,
// 例如 ItemDamage, ItemMaxDamage.

/**
 * 模板的“类型”. 该接口有两个作用:
 *
 * - 充当 [TypeSerializer]
 * - 作为 [ItemTemplateMap] 的索引
 *
 * @param T 物品模板的类型, 必须实现 [ItemTemplate]
 */
interface ItemTemplateType<T> : Examinable {
    /**
     * 模板的唯一标识.
     */
    val id: String

    /**
     * 该模板的类型.
     */
    val type: TypeToken<T>

    /**
     * 定义如何将 [node] 反序列化为 [T].
     */
    fun decode(node: ConfigurationNode): T

    // /**
    //  * 定义如何将 [T] 写入到 [node].
    //  */
    // fun encode(obj: T?, node: ConfigurationNode) {
    //     throw UnsupportedOperationException()
    // }

    /**
     * 该序列化会用到的子序列化器.
     *
     * 当一个序列化实现非常复杂时, 拆分实现是个好习惯.
     *
     * 默认返回空集合, 意为该组件没有子序列化器.
     */
    fun childrenCodecs(): TypeSerializerCollection {
        return TypeSerializerCollection.builder().build()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
    )
}