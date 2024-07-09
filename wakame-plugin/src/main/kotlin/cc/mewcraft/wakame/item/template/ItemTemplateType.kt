package cc.mewcraft.wakame.item.template

import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type
import java.util.stream.Stream

// 开发日记 2024/6/26
// 之所以叫 Kind 不叫 Type 是因为 Type 跟 serialize 里的函数参数名冲突了.
// 而之所以叫 Kind 不叫 Serializer 是因为这将作为 ItemTemplate 的映射键.
//
// 开发日记 2024/7/5
// 这里的类型 T 不能设置 upper bounds,
// 因为存在数据类型为 primitive 的组件,
// 例如 ItemDamage, ItemMaxDamage, ItemTracks, SystemUse.

/**
 * 模板的“类型”. 该接口有两个作用:
 *
 * - 充当 [TypeSerializer]
 * - 作为 [ItemTemplateMap] 的索引
 *
 * @param T 物品模板的类型
 */
interface ItemTemplateType<T : ItemTemplate<*>> : Examinable {
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

    /**
     * 定义如何将 [T] 序列化到 [node].
     */
    fun encode(type: Type, obj: T?, node: ConfigurationNode): Nothing {
        throw UnsupportedOperationException()
    }

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
        ExaminableProperty.of("id", id)
    )
}