package cc.mewcraft.wakame.item.template

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

// 开发日记 2024/6/26
// 之所以叫 Kind 不叫 Type 是因为 Type 跟 serialize 里的函数参数名冲突了.
// 而之所以叫 Kind 不叫 Serializer 是因为这将作为 ItemTemplate 的映射键.

/**
 * 模板的“类型”. 该接口有两个作用:
 *
 * - 充当 [TypeSerializer]
 * - 作为 [ItemTemplateMap] 的索引
 */
interface ItemTemplateType<T : ItemTemplate<*>> : TypeSerializer<T> {
    override fun deserialize(type: Type, node: ConfigurationNode): T

    override fun serialize(type: Type, obj: T?, node: ConfigurationNode): Nothing {
        throw UnsupportedOperationException()
    }

    /**
     * 如果该模板在缺省时需要有个默认值,
     * 那么该函数必须返回一个非空的值.
     * 返回的值将作为默认值使用.
     *
     * 默认返回 `null`, 意为该组件没有默认模板.
     */
    override fun emptyValue(specificType: Type?, options: ConfigurationOptions?): T? {
        return null
    }
}