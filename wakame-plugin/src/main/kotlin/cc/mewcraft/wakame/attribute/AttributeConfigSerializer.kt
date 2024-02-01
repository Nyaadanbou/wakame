package cc.mewcraft.wakame.attribute

import org.spongepowered.configurate.ConfigurationNode

/**
 * 属性在配置文件中的序列化操作。
 *
 * @param T 属性的类型，详见 [AttributeSchemaValue] 的实现类
 */
interface AttributeConfigSerializer<T : AttributeSchemaValue> {
    /**
     * 将 [value] 写进 [node]
     */
    fun serialize(node: ConfigurationNode, value: T)

    /**
     * 读取 [node] 并返回 [T]
     */
    fun deserialize(node: ConfigurationNode): T
}