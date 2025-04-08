package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.entity.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetype
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode


/**
 * 从配置文件构建 [AttributeCoreArchetype].
 *
 * @param id 核心的唯一标识, 也就是 [CoreArchetype.id]
 * @param node 包含该核心数据的配置节点
 * @return 从配置文件构建的 [AttributeCoreArchetype]
 */
fun AttributeCoreArchetype(
    id: Key,
    node: ConfigurationNode,
): AttributeCoreArchetype = SimpleAttributeCoreArchetype(
    id = id, data = BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(id.value()).convertNodeToVariable(node)
)

/**
 * 代表属性核心 [AttributeCore] 的模板.
 */
interface AttributeCoreArchetype : CoreArchetype {
    /**
     * 属性核心的模板.
     */
    val data: VariableAttributeBundle

    /**
     * 生成一个 [AttributeCore] 实例.
     *
     * @param context 物品生成的上下文
     * @return 生成的 [AttributeCore]
     */
    override fun generate(context: ItemGenerationContext): AttributeCore
}

/**
 * [AttributeCoreArchetype] 的标准实现.
 */
private class SimpleAttributeCoreArchetype(
    override val id: Key,
    override val data: VariableAttributeBundle,
) : AttributeCoreArchetype {

    override fun generate(context: ItemGenerationContext): AttributeCore {
        val id = id
        val data = data.generate(context)
        return AttributeCore(id, data)
    }

}
