package cc.mewcraft.wakame.item.templates.components.cells.cores

import cc.mewcraft.wakame.attribute.composite.VariableCompositeAttribute
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.cores.AttributeCore
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.templates.components.cells.CoreArchetype
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.collections.mapToArray
import java.util.stream.Stream


/**
 * 从配置文件构建 [AttributeCoreArchetype].
 *
 * @param id 核心的唯一标识, 也就是 [CoreArchetype.id]
 * @param node 包含该核心数据的配置节点
 *
 * @return 从配置文件构建的 [AttributeCoreArchetype]
 */
fun AttributeCoreArchetype(
    id: Key,
    node: ConfigurationNode,
): AttributeCoreArchetype = SimpleAttributeCoreArchetype(
    id = id,
    attribute = AttributeRegistry.FACADES[id.value()].convertNode2Variable(node)
)

/**
 * 代表属性核心 [AttributeCore] 的模板.
 */
interface AttributeCoreArchetype : CoreArchetype {
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
 * [AttributeCoreArchetype] 的标准实现.
 */
internal class SimpleAttributeCoreArchetype(
    override val id: Key,
    override val attribute: VariableCompositeAttribute,
) : AttributeCoreArchetype {
    override fun generate(context: ItemGenerationContext): AttributeCore {
        val (attribute, scores) = attribute.generate(context)
        return AttributeCore(
            id = id,
            attribute = attribute,
            quality = scores.mapToArray(AttributeCore.Quality::fromZScore)
        )
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
