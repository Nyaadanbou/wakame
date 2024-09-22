package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.attribute.composite.VariableCompositeAttribute
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.cores.AttributeCore
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.CoreBlueprint
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream


/**
 * 从配置文件构建 [AttributeCoreBlueprint].
 *
 * @param id 核心的唯一标识, 也就是 [CoreBlueprint.id]
 * @param node 包含该核心数据的配置节点
 *
 * @return 从配置文件构建的 [AttributeCoreBlueprint]
 */
fun AttributeCoreBlueprint(
    id: Key,
    node: ConfigurationNode
): AttributeCoreBlueprint {
    val compositeAttributeId = id.value()
    val compositeAttribute = AttributeRegistry.FACADES[compositeAttributeId].convertNode2Variable(node)
    return SimpleAttributeCoreBlueprint(id, compositeAttribute)
}

/**
 * 代表属性核心 [AttributeCore] 的模板.
 */
interface AttributeCoreBlueprint : CoreBlueprint {
    /**
     * 属性核心的模板.
     */
    val attribute: VariableCompositeAttribute

    /**
     * 生成一个 [AttributeCore] 实例.
     *
     * @param context 物品生成的上下文
     * @return 生成的 [AttributeCore]
     */
    override fun generate(context: ItemGenerationContext): AttributeCore
}

/**
 * [AttributeCoreBlueprint] 的标准实现.
 */
internal class SimpleAttributeCoreBlueprint(
    override val id: Key,
    override val attribute: VariableCompositeAttribute,
) : AttributeCoreBlueprint {
    override fun generate(context: ItemGenerationContext): AttributeCore {
        val compositeAttribute = attribute.generate(context)
        return AttributeCore(id, compositeAttribute)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("attribute", attribute)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
