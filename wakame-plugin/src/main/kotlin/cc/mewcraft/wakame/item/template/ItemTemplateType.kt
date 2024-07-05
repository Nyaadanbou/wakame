package cc.mewcraft.wakame.item.template

import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type
import java.util.stream.Stream

// 开发日记 2024/6/26
// 之所以叫 Kind 不叫 Type 是因为 Type 跟 serialize 里的函数参数名冲突了.
// 而之所以叫 Kind 不叫 Serializer 是因为这将作为 ItemTemplate 的映射键.

/**
 * 模板的“类型”. 该接口有两个作用:
 *
 * - 充当 [TypeSerializer]
 * - 作为 [ItemTemplateMap] 的索引
 *
 * @param T 物品组件(数据)的类型
 */
interface ItemTemplateType<T> : TypeSerializer<ItemTemplate<T>>, Examinable {
    /**
     * The [TypeToken] of [T].
     */
    val typeToken: TypeToken<ItemTemplate<T>> // generic sucks :x

    /**
     * 该序列化会用到的子序列化器.
     *
     * 当一个序列化实现非常复杂时, 拆分实现是个好习惯.
     *
     * 默认返回空集合, 意为该组件没有子序列化器.
     */
    fun childSerializers(): TypeSerializerCollection {
        return TypeSerializerCollection.builder().build()
    }

    /**
     * 定义如何将 [node] 反序列化为 [T].
     */
    override fun deserialize(type: Type, node: ConfigurationNode): ItemTemplate<T>

    /**
     * 定义如何将 [T] 序列化到 [node].
     */
    override fun serialize(type: Type, obj: ItemTemplate<T>?, node: ConfigurationNode): Nothing {
        throw UnsupportedOperationException()
    }

    /**
     * 如果该模板在缺省时需要有个默认值,
     * 那么该函数必须返回一个非空的值.
     * 返回的值将作为默认值使用.
     *
     * 默认返回 `null`, 意为该组件没有默认模板.
     */
    override fun emptyValue(specificType: Type?, options: ConfigurationOptions?): ItemTemplate<T>? {
        return null
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("type", typeToken.type)
    )
}